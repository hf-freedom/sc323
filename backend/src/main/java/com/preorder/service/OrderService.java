package com.preorder.service;

import com.preorder.entity.PreorderOrder;
import com.preorder.entity.PreorderProduct;
import com.preorder.entity.User;
import com.preorder.enums.OrderStatus;
import com.preorder.store.InMemoryStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    public PreorderOrder createDepositOrder(Long productId, Long userId, Integer quantity) {
        PreorderProduct product = InMemoryStore.getProduct(productId);
        if (product == null || !product.getActive()) {
            throw new RuntimeException("预售商品不存在或已下架");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(product.getDepositStartTime()) || now.isAfter(product.getDepositEndTime())) {
            throw new RuntimeException("不在定金支付时间范围内");
        }

        int availableStock = product.getTotalStock() - product.getLockedStock();
        if (availableStock < quantity) {
            throw new RuntimeException("预售库存不足，剩余库存：" + availableStock);
        }

        User user = InMemoryStore.getUser(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        BigDecimal depositAmount = product.getDeposit().multiply(BigDecimal.valueOf(quantity));
        if (user.getBalance().compareTo(depositAmount) < 0) {
            throw new RuntimeException("余额不足");
        }

        product.setLockedStock(product.getLockedStock() + quantity);
        InMemoryStore.saveProduct(product);

        PreorderOrder order = new PreorderOrder();
        order.setId(InMemoryStore.ORDER_ID_GENERATOR.getAndIncrement());
        order.setOrderNo("PO" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        order.setUserId(userId);
        order.setUserName(user.getNickname());
        order.setProductId(productId);
        order.setProductName(product.getName());
        order.setProductImage(product.getImage());
        order.setQuantity(quantity);
        order.setOriginalPrice(product.getOriginalPrice());
        order.setPreorderPrice(product.getPreorderPrice());
        order.setDeposit(product.getDeposit());
        order.setBalance(product.getPreorderPrice().subtract(product.getDeposit()));
        order.setTotalAmount(product.getPreorderPrice().multiply(BigDecimal.valueOf(quantity)));
        order.setDepositPaid(BigDecimal.ZERO);
        order.setBalancePaid(BigDecimal.ZERO);
        order.setRefundAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.DEPOSIT_PENDING);
        order.setDepositStartTime(product.getDepositStartTime());
        order.setDepositEndTime(product.getDepositEndTime());
        order.setBalanceStartTime(product.getBalanceStartTime());
        order.setBalanceEndTime(product.getBalanceEndTime());
        order.setCreateTime(now);
        order.setUpdateTime(now);

        InMemoryStore.saveOrder(order);
        return order;
    }

    public PreorderOrder payDeposit(String orderNo) {
        PreorderOrder order = InMemoryStore.getOrder(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != OrderStatus.DEPOSIT_PENDING) {
            throw new RuntimeException("当前订单状态不支持支付定金");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(order.getDepositEndTime())) {
            order.setStatus(OrderStatus.DEPOSIT_TIMEOUT);
            order.setCancelReason("定金支付超时");
            order.setCancelTime(now);
            releaseStock(order);
            InMemoryStore.saveOrder(order);
            throw new RuntimeException("定金支付已超时");
        }

        User user = InMemoryStore.getUser(order.getUserId());
        BigDecimal depositAmount = order.getDeposit().multiply(BigDecimal.valueOf(order.getQuantity()));

        if (user.getBalance().compareTo(depositAmount) < 0) {
            throw new RuntimeException("余额不足");
        }

        user.setBalance(user.getBalance().subtract(depositAmount));

        order.setDepositPaid(depositAmount);
        order.setStatus(OrderStatus.DEPOSIT_PAID);
        order.setDepositPayTime(now);
        InMemoryStore.saveOrder(order);

        return order;
    }

    public PreorderOrder payBalance(String orderNo) {
        PreorderOrder order = InMemoryStore.getOrder(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != OrderStatus.BALANCE_PERIOD) {
            throw new RuntimeException("当前订单状态不支持支付尾款");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(order.getBalanceStartTime())) {
            throw new RuntimeException("尾款支付尚未开始");
        }
        if (now.isAfter(order.getBalanceEndTime())) {
            order.setStatus(OrderStatus.BALANCE_TIMEOUT);
            order.setCancelReason("尾款支付超时");
            order.setCancelTime(now);
            processBalanceTimeout(order);
            InMemoryStore.saveOrder(order);
            throw new RuntimeException("尾款支付已超时");
        }

        User user = InMemoryStore.getUser(order.getUserId());
        BigDecimal balanceAmount = order.getBalance().multiply(BigDecimal.valueOf(order.getQuantity()));

        if (user.getBalance().compareTo(balanceAmount) < 0) {
            throw new RuntimeException("余额不足");
        }

        user.setBalance(user.getBalance().subtract(balanceAmount));

        order.setBalancePaid(balanceAmount);
        order.setStatus(OrderStatus.BALANCE_PAID);
        order.setBalancePayTime(now);

        PreorderProduct product = InMemoryStore.getProduct(order.getProductId());
        if (product != null) {
            product.setSoldStock(product.getSoldStock() + order.getQuantity());
            product.setLockedStock(product.getLockedStock() - order.getQuantity());
            InMemoryStore.saveProduct(product);
        }

        InMemoryStore.saveOrder(order);
        return order;
    }

    public PreorderOrder cancelOrder(String orderNo, String reason) {
        PreorderOrder order = InMemoryStore.getOrder(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() == OrderStatus.BALANCE_PAID ||
            order.getStatus() == OrderStatus.SHIPPED ||
            order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("当前订单状态不支持取消，请申请退款");
        }

        if (order.getStatus() == OrderStatus.CANCELLED ||
            order.getStatus() == OrderStatus.DEPOSIT_TIMEOUT ||
            order.getStatus() == OrderStatus.BALANCE_TIMEOUT) {
            return order;
        }

        LocalDateTime now = LocalDateTime.now();
        User user = InMemoryStore.getUser(order.getUserId());

        if (order.getStatus() == OrderStatus.DEPOSIT_PAID) {
            user.setBalance(user.getBalance().add(order.getDepositPaid()));
        } else if (order.getStatus() == OrderStatus.BALANCE_PERIOD) {
            user.setBalance(user.getBalance().add(order.getDepositPaid()));
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelTime(now);
        order.setCancelReason(StringUtils.hasText(reason) ? reason : "用户取消");
        releaseStock(order);
        InMemoryStore.saveOrder(order);

        return order;
    }

    public BigDecimal calculateRefundAmount(String orderNo) {
        PreorderOrder order = InMemoryStore.getOrder(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        BigDecimal totalPaid = order.getDepositPaid().add(order.getBalancePaid());

        if (order.getStatus() == OrderStatus.DEPOSIT_PAID) {
            return order.getDepositPaid();
        } else if (order.getStatus() == OrderStatus.BALANCE_PERIOD) {
            return order.getDepositPaid();
        } else if (order.getStatus() == OrderStatus.BALANCE_PAID ||
                   order.getStatus() == OrderStatus.SHIPPED) {
            return totalPaid;
        } else if (order.getStatus() == OrderStatus.COMPLETED) {
            return totalPaid;
        }

        return BigDecimal.ZERO;
    }

    public PreorderOrder applyRefund(String orderNo, String reason) {
        PreorderOrder order = InMemoryStore.getOrder(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != OrderStatus.BALANCE_PAID &&
            order.getStatus() != OrderStatus.SHIPPED &&
            order.getStatus() != OrderStatus.COMPLETED &&
            order.getStatus() != OrderStatus.DEPOSIT_PAID &&
            order.getStatus() != OrderStatus.BALANCE_PERIOD) {
            throw new RuntimeException("当前订单状态不支持退款");
        }

        BigDecimal refundAmount = calculateRefundAmount(orderNo);
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("无可退款金额");
        }

        User user = InMemoryStore.getUser(order.getUserId());
        user.setBalance(user.getBalance().add(refundAmount));

        order.setRefundAmount(refundAmount);
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason("退款：" + (StringUtils.hasText(reason) ? reason : "用户申请退款"));

        if (order.getStatus() == OrderStatus.BALANCE_PAID ||
            order.getStatus() == OrderStatus.SHIPPED ||
            order.getStatus() == OrderStatus.COMPLETED) {
            PreorderProduct product = InMemoryStore.getProduct(order.getProductId());
            if (product != null) {
                product.setSoldStock(Math.max(0, product.getSoldStock() - order.getQuantity()));
                product.setTotalStock(product.getTotalStock() + order.getQuantity());
                InMemoryStore.saveProduct(product);
            }
        } else {
            releaseStock(order);
        }

        InMemoryStore.saveOrder(order);
        return order;
    }

    private void releaseStock(PreorderOrder order) {
        PreorderProduct product = InMemoryStore.getProduct(order.getProductId());
        if (product != null) {
            product.setLockedStock(Math.max(0, product.getLockedStock() - order.getQuantity()));
            InMemoryStore.saveProduct(product);
        }
    }

    public void processBalanceTimeout(PreorderOrder order) {
        releaseStock(order);
    }

    public void endPreorderActivity(Long productId) {
        PreorderProduct product = InMemoryStore.getProduct(productId);
        if (product == null) {
            throw new RuntimeException("预售商品不存在");
        }

        product.setActive(false);
        product.setDepositEndTime(LocalDateTime.now());
        product.setBalanceEndTime(LocalDateTime.now().plusHours(24));
        InMemoryStore.saveProduct(product);

        LocalDateTime now = LocalDateTime.now();
        for (PreorderOrder order : InMemoryStore.getAllOrders()) {
            if (order.getProductId().equals(productId)) {
                if (order.getStatus() == OrderStatus.DEPOSIT_PAID) {
                    order.setBalanceStartTime(now);
                    order.setBalanceEndTime(now.plusHours(24));
                    order.setStatus(OrderStatus.BALANCE_PERIOD);
                    InMemoryStore.saveOrder(order);
                } else if (order.getStatus() == OrderStatus.DEPOSIT_PENDING) {
                    order.setStatus(OrderStatus.CANCELLED);
                    order.setCancelTime(now);
                    order.setCancelReason("预售活动提前结束");
                    InMemoryStore.saveOrder(order);
                }
            }
        }
    }

    public List<PreorderOrder> getOrdersByUserId(Long userId) {
        return InMemoryStore.getAllOrders().stream()
                .filter(o -> o.getUserId().equals(userId))
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .collect(Collectors.toList());
    }

    public List<PreorderOrder> getAllOrders() {
        return InMemoryStore.getAllOrders().stream()
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .collect(Collectors.toList());
    }

    public PreorderOrder getOrder(String orderNo) {
        return InMemoryStore.getOrder(orderNo);
    }

    public PreorderOrder deliverOrder(String orderNo) {
        PreorderOrder order = InMemoryStore.getOrder(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.BALANCE_PAID) {
            throw new RuntimeException("当前订单状态不支持发货");
        }
        order.setStatus(OrderStatus.SHIPPED);
        order.setDeliveryTime(LocalDateTime.now());
        InMemoryStore.saveOrder(order);
        return order;
    }

    public PreorderOrder completeOrder(String orderNo) {
        PreorderOrder order = InMemoryStore.getOrder(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("当前订单状态不支持确认收货");
        }
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompleteTime(LocalDateTime.now());
        InMemoryStore.saveOrder(order);
        return order;
    }
}
