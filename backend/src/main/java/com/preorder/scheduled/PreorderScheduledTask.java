package com.preorder.scheduled;

import com.preorder.entity.PreorderOrder;
import com.preorder.entity.PreorderProduct;
import com.preorder.enums.OrderStatus;
import com.preorder.store.InMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PreorderScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(PreorderScheduledTask.class);

    @Scheduled(fixedDelayString = "${preorder.scheduled.fixed-delay:5000}")
    public void processPreorderTasks() {
        LocalDateTime now = LocalDateTime.now();
        processBalanceStart(now);
        processDepositTimeout(now);
        processBalanceTimeout(now);
    }

    private void processBalanceStart(LocalDateTime now) {
        for (PreorderOrder order : InMemoryStore.getAllOrders()) {
            if (order.getStatus() == OrderStatus.DEPOSIT_PAID
                    && order.getBalanceStartTime() != null
                    && !now.isBefore(order.getBalanceStartTime())) {
                order.setStatus(OrderStatus.BALANCE_PERIOD);
                order.setUpdateTime(now);
                InMemoryStore.saveOrder(order);
                logger.info("订单 {} 进入尾款期", order.getOrderNo());
            }
        }
    }

    private void processDepositTimeout(LocalDateTime now) {
        for (PreorderOrder order : InMemoryStore.getAllOrders()) {
            if (order.getStatus() == OrderStatus.DEPOSIT_PENDING
                    && order.getDepositEndTime() != null
                    && now.isAfter(order.getDepositEndTime())) {
                order.setStatus(OrderStatus.DEPOSIT_TIMEOUT);
                order.setCancelTime(now);
                order.setCancelReason("定金支付超时");
                order.setUpdateTime(now);

                PreorderProduct product = InMemoryStore.getProduct(order.getProductId());
                if (product != null) {
                    product.setLockedStock(Math.max(0, product.getLockedStock() - order.getQuantity()));
                    InMemoryStore.saveProduct(product);
                }

                InMemoryStore.saveOrder(order);
                logger.info("订单 {} 定金支付超时，已释放库存", order.getOrderNo());
            }
        }
    }

    private void processBalanceTimeout(LocalDateTime now) {
        for (PreorderOrder order : InMemoryStore.getAllOrders()) {
            if (order.getStatus() == OrderStatus.BALANCE_PERIOD
                    && order.getBalanceEndTime() != null
                    && now.isAfter(order.getBalanceEndTime())) {
                order.setStatus(OrderStatus.BALANCE_TIMEOUT);
                order.setCancelTime(now);
                order.setCancelReason("尾款支付超时，定金不予退还");
                order.setUpdateTime(now);

                PreorderProduct product = InMemoryStore.getProduct(order.getProductId());
                if (product != null) {
                    product.setLockedStock(Math.max(0, product.getLockedStock() - order.getQuantity()));
                    InMemoryStore.saveProduct(product);
                }

                InMemoryStore.saveOrder(order);
                logger.info("订单 {} 尾款支付超时，定金不予退还，已释放库存", order.getOrderNo());
            }
        }
    }
}
