package com.preorder.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PreorderProduct {
    private Long id;
    private String name;
    private String description;
    private String image;
    private BigDecimal originalPrice;
    private BigDecimal preorderPrice;
    private BigDecimal deposit;
    private Integer totalStock;
    private Integer lockedStock;
    private Integer soldStock;
    private LocalDateTime depositStartTime;
    private LocalDateTime depositEndTime;
    private LocalDateTime balanceStartTime;
    private LocalDateTime balanceEndTime;
    private Boolean active;
    private String depositRule;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public BigDecimal getPreorderPrice() { return preorderPrice; }
    public void setPreorderPrice(BigDecimal preorderPrice) { this.preorderPrice = preorderPrice; }
    public BigDecimal getDeposit() { return deposit; }
    public void setDeposit(BigDecimal deposit) { this.deposit = deposit; }
    public Integer getTotalStock() { return totalStock; }
    public void setTotalStock(Integer totalStock) { this.totalStock = totalStock; }
    public Integer getLockedStock() { return lockedStock; }
    public void setLockedStock(Integer lockedStock) { this.lockedStock = lockedStock; }
    public Integer getSoldStock() { return soldStock; }
    public void setSoldStock(Integer soldStock) { this.soldStock = soldStock; }
    public LocalDateTime getDepositStartTime() { return depositStartTime; }
    public void setDepositStartTime(LocalDateTime depositStartTime) { this.depositStartTime = depositStartTime; }
    public LocalDateTime getDepositEndTime() { return depositEndTime; }
    public void setDepositEndTime(LocalDateTime depositEndTime) { this.depositEndTime = depositEndTime; }
    public LocalDateTime getBalanceStartTime() { return balanceStartTime; }
    public void setBalanceStartTime(LocalDateTime balanceStartTime) { this.balanceStartTime = balanceStartTime; }
    public LocalDateTime getBalanceEndTime() { return balanceEndTime; }
    public void setBalanceEndTime(LocalDateTime balanceEndTime) { this.balanceEndTime = balanceEndTime; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getDepositRule() { return depositRule; }
    public void setDepositRule(String depositRule) { this.depositRule = depositRule; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
