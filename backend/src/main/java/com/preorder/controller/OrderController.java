package com.preorder.controller;

import com.preorder.common.Result;
import com.preorder.entity.PreorderOrder;
import com.preorder.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/deposit")
    public Result<PreorderOrder> createDepositOrder(@RequestBody Map<String, Object> params) {
        try {
            Long productId = Long.valueOf(params.get("productId").toString());
            Long userId = Long.valueOf(params.get("userId").toString());
            Integer quantity = params.get("quantity") != null ?
                    Integer.valueOf(params.get("quantity").toString()) : 1;
            PreorderOrder order = orderService.createDepositOrder(productId, userId, quantity);
            return Result.success("订单创建成功", order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{orderNo}/pay-deposit")
    public Result<PreorderOrder> payDeposit(@PathVariable String orderNo) {
        try {
            PreorderOrder order = orderService.payDeposit(orderNo);
            return Result.success("定金支付成功", order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{orderNo}/pay-balance")
    public Result<PreorderOrder> payBalance(@PathVariable String orderNo) {
        try {
            PreorderOrder order = orderService.payBalance(orderNo);
            return Result.success("尾款支付成功", order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{orderNo}/cancel")
    public Result<PreorderOrder> cancelOrder(@PathVariable String orderNo,
                                             @RequestBody(required = false) Map<String, String> params) {
        try {
            String reason = params != null ? params.get("reason") : null;
            PreorderOrder order = orderService.cancelOrder(orderNo, reason);
            return Result.success("订单取消成功", order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{orderNo}/refund-amount")
    public Result<BigDecimal> getRefundAmount(@PathVariable String orderNo) {
        try {
            BigDecimal amount = orderService.calculateRefundAmount(orderNo);
            return Result.success(amount);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{orderNo}/refund")
    public Result<PreorderOrder> applyRefund(@PathVariable String orderNo,
                                             @RequestBody(required = false) Map<String, String> params) {
        try {
            String reason = params != null ? params.get("reason") : null;
            PreorderOrder order = orderService.applyRefund(orderNo, reason);
            return Result.success("退款申请成功", order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{orderNo}/deliver")
    public Result<PreorderOrder> deliverOrder(@PathVariable String orderNo) {
        try {
            PreorderOrder order = orderService.deliverOrder(orderNo);
            return Result.success("发货成功", order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{orderNo}/complete")
    public Result<PreorderOrder> completeOrder(@PathVariable String orderNo) {
        try {
            PreorderOrder order = orderService.completeOrder(orderNo);
            return Result.success("确认收货成功", order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public Result<List<PreorderOrder>> getOrdersByUserId(@PathVariable Long userId) {
        return Result.success(orderService.getOrdersByUserId(userId));
    }

    @GetMapping
    public Result<List<PreorderOrder>> getAllOrders() {
        return Result.success(orderService.getAllOrders());
    }

    @GetMapping("/{orderNo}")
    public Result<PreorderOrder> getOrder(@PathVariable String orderNo) {
        PreorderOrder order = orderService.getOrder(orderNo);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @PostMapping("/end-activity/{productId}")
    public Result<Void> endPreorderActivity(@PathVariable Long productId) {
        try {
            orderService.endPreorderActivity(productId);
            return Result.success("预售活动已提前结束", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
