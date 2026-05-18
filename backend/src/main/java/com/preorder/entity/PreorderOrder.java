package com.preorder.entity;

import com.preorder.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PreorderOrder {
    private Long id;
    private String orderNo;
    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal originalPrice;
    private BigDecimal preorderPrice;
    private BigDecimal deposit;
    private BigDecimal balance;
    private BigDecimal totalAmount;
    private BigDecimal depositPaid;
    private BigDecimal balancePaid;
    private BigDecimal refundAmount;
    private OrderStatus status;
    private LocalDateTime depositPayTime;
    private LocalDateTime balancePayTime;
    private LocalDateTime depositStartTime;
    private LocalDateTime depositEndTime;
    private LocalDateTime balanceStartTime;
    private LocalDateTime balanceEndTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private LocalDateTime deliveryTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public BigDecimal getPreorderPrice() { return preorderPrice; }
    public void setPreorderPrice(BigDecimal preorderPrice) { this.preorderPrice = preorderPrice; }
    public BigDecimal getDeposit() { return deposit; }
    public void setDeposit(BigDecimal deposit) { this.deposit = deposit; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDepositPaid() { return depositPaid; }
    public void setDepositPaid(BigDecimal depositPaid) { this.depositPaid = depositPaid; }
    public BigDecimal getBalancePaid() { return balancePaid; }
    public void setBalancePaid(BigDecimal balancePaid) { this.balancePaid = balancePaid; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public LocalDateTime getDepositPayTime() { return depositPayTime; }
    public void setDepositPayTime(LocalDateTime depositPayTime) { this.depositPayTime = depositPayTime; }
    public LocalDateTime getBalancePayTime() { return balancePayTime; }
    public void setBalancePayTime(LocalDateTime balancePayTime) { this.balancePayTime = balancePayTime; }
    public LocalDateTime getDepositStartTime() { return depositStartTime; }
    public void setDepositStartTime(LocalDateTime depositStartTime) { this.depositStartTime = depositStartTime; }
    public LocalDateTime getDepositEndTime() { return depositEndTime; }
    public void setDepositEndTime(LocalDateTime depositEndTime) { this.depositEndTime = depositEndTime; }
    public LocalDateTime getBalanceStartTime() { return balanceStartTime; }
    public void setBalanceStartTime(LocalDateTime balanceStartTime) { this.balanceStartTime = balanceStartTime; }
    public LocalDateTime getBalanceEndTime() { return balanceEndTime; }
    public void setBalanceEndTime(LocalDateTime balanceEndTime) { this.balanceEndTime = balanceEndTime; }
    public LocalDateTime getCancelTime() { return cancelTime; }
    public void setCancelTime(LocalDateTime cancelTime) { this.cancelTime = cancelTime; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public LocalDateTime getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(LocalDateTime deliveryTime) { this.deliveryTime = deliveryTime; }
    public LocalDateTime getCompleteTime() { return completeTime; }
    public void setCompleteTime(LocalDateTime completeTime) { this.completeTime = completeTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
