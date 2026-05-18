<template>
  <div class="order-list">
    <h2>我的订单</h2>
    
    <el-collapse class="activity-collapse" v-if="allProducts.length > 0">
      <el-collapse-item name="1">
        <template slot="title">
          <i class="el-icon-setting"></i> 预售活动管理
        </template>
        <div class="activity-list">
          <div v-for="product in allProducts" :key="product.id" class="activity-item">
            <div class="activity-info">
              <span class="activity-name">{{ product.name }}</span>
              <el-tag :type="product.active ? 'success' : 'info'" size="mini">
                {{ product.active ? '进行中' : '已结束' }}
              </el-tag>
              <span class="activity-stock">库存: {{ product.totalStock - product.lockedStock }}/{{ product.totalStock }}</span>
            </div>
            <el-button
              v-if="product.active"
              type="danger"
              size="mini"
              @click="endPreorderActivity(product)"
            >
              提前结束活动
            </el-button>
          </div>
        </div>
      </el-collapse-item>
    </el-collapse>

    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="全部订单" name="all"></el-tab-pane>
      <el-tab-pane label="定金期" name="deposit"></el-tab-pane>
      <el-tab-pane label="尾款期" name="balance"></el-tab-pane>
      <el-tab-pane label="待发货" name="pending"></el-tab-pane>
      <el-tab-pane label="已完成" name="completed"></el-tab-pane>
      <el-tab-pane label="已取消" name="cancelled"></el-tab-pane>
    </el-tabs>

    <div v-if="filteredOrders.length === 0" class="empty">
      <el-empty description="暂无订单" />
    </div>

    <el-card
      v-for="order in filteredOrders"
      :key="order.orderNo"
      class="order-card"
      :class="{ 'balance-period-card': order.status === 'BALANCE_PERIOD' }"
    >
      <div class="order-header">
        <span class="order-no">订单号: {{ order.orderNo }}</span>
        <el-tag :type="getStatusType(order.status)" size="small" effect="dark">
          {{ getStatusText(order.status) }}
        </el-tag>
        <span class="order-time">{{ formatDate(order.createTime) }}</span>
      </div>
      
      <div v-if="order.status === 'BALANCE_PERIOD'" class="balance-notice">
        <i class="el-icon-warning"></i>
        <span>尾款支付已开放！请在 {{ formatDate(order.balanceEndTime) }} 前完成支付，逾期定金不予退还</span>
        <span class="countdown" v-if="getCountdown(order.balanceEndTime)">
          剩余: {{ getCountdown(order.balanceEndTime) }}
        </span>
      </div>

      <div class="order-content">
        <img :src="order.productImage" class="product-image" />
        <div class="product-info">
          <h3>{{ order.productName }}</h3>
          <p>数量: {{ order.quantity }}</p>
          <p>单价: ¥{{ order.preorderPrice }}</p>
          <p class="order-total">订单总额: <span class="price">¥{{ order.totalAmount }}</span></p>
        </div>
        <div class="payment-info">
          <p v-if="order.depositPaid > 0">已付定金: <span class="deposit">¥{{ order.depositPaid }}</span></p>
          <p v-if="order.balancePaid > 0">已付尾款: <span class="balance">¥{{ order.balancePaid }}</span></p>
          <div v-if="order.status === 'BALANCE_PERIOD'" class="balance-payment-box">
            <p class="payable-label">应付尾款:</p>
            <p class="payable-amount">¥{{ (order.balance * order.quantity).toFixed(2) }}</p>
            <p class="payment-detail">
              (订单总额 ¥{{ order.totalAmount }} - 已付定金 ¥{{ order.depositPaid }})
            </p>
          </div>
          <p v-if="order.status === 'DEPOSIT_PAID'" class="deposit-period-notice">
            <i class="el-icon-time"></i>
            尾款支付将于 {{ formatDate(order.balanceStartTime) }} 开放
          </p>
          <p v-if="order.refundAmount > 0">已退款: <span class="refund">¥{{ order.refundAmount }}</span></p>
          <p v-if="order.cancelReason" class="cancel-reason">{{ order.cancelReason }}</p>
        </div>
        <div class="order-actions">
          <el-button
            v-if="order.status === 'BALANCE_PERIOD'"
            type="danger"
            size="medium"
            class="pay-balance-btn"
            @click="payBalance(order)"
          >
            <i class="el-icon-credit-pay"></i> 立即支付尾款
          </el-button>
          <el-button
            v-if="order.status === 'DEPOSIT_PAID' || order.status === 'BALANCE_PERIOD'"
            @click="cancelOrder(order)"
          >
            取消订单
          </el-button>
          <el-button
            v-if="order.status === 'DEPOSIT_PAID' || order.status === 'BALANCE_PERIOD' || order.status === 'BALANCE_PAID' || order.status === 'SHIPPED' || order.status === 'COMPLETED'"
            @click="applyRefund(order)"
          >
            申请退款
          </el-button>
          <el-button
            v-if="order.status === 'SHIPPED'"
            type="success"
            @click="confirmReceive(order)"
          >
            确认收货
          </el-button>
        </div>
      </div>
      <div class="order-footer">
        <p v-if="order.status === 'DEPOSIT_PAID' || order.status === 'BALANCE_PERIOD'">
          <i class="el-icon-time"></i>
          尾款支付时间: {{ formatDate(order.balanceStartTime) }} ~ {{ formatDate(order.balanceEndTime) }}
        </p>
      </div>
    </el-card>

    <el-dialog title="支付尾款" :visible.sync="balanceDialogVisible" width="400px">
      <div v-if="selectedOrder" class="balance-form">
        <p>商品: {{ selectedOrder.productName }}</p>
        <p>数量: {{ selectedOrder.quantity }}</p>
        <p>已付定金: ¥{{ selectedOrder.depositPaid }}</p>
        <p class="pay-amount">应付尾款: <span>¥{{ (selectedOrder.balance * selectedOrder.quantity).toFixed(2) }}</span></p>
      </div>
      <span slot="footer">
        <el-button @click="balanceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmPayBalance" :loading="paying">确认支付</el-button>
      </span>
    </el-dialog>

    <el-dialog title="申请退款" :visible.sync="refundDialogVisible" width="500px">
      <div v-if="selectedOrder" class="refund-form">
        <div class="refund-header">
          <img :src="selectedOrder.productImage" class="refund-product-image" />
          <div class="refund-product-info">
            <h4>{{ selectedOrder.productName }}</h4>
            <p>数量: {{ selectedOrder.quantity }}</p>
          </div>
        </div>

        <el-divider content-position="left">
          <span class="divider-title">当前订单阶段</span>
        </el-divider>
        
        <div class="order-stage-badge">
          <el-tag :type="getOrderStageType(selectedOrder.status)" effect="dark" size="medium">
            {{ getOrderStageText(selectedOrder.status) }}
          </el-tag>
        </div>

        <el-divider content-position="left">
          <span class="divider-title">退款计算明细</span>
        </el-divider>

        <div class="refund-calculation">
          <div class="refund-row">
            <span class="label">订单总额:</span>
            <span class="value">¥{{ selectedOrder.totalAmount }}</span>
          </div>
          
          <div class="refund-row deposit-section">
            <div class="section-header">
              <i class="el-icon-s-finance"></i>
              <span>定金部分</span>
            </div>
            <div class="section-content">
              <div class="refund-row">
                <span class="label">已支付定金:</span>
                <span class="value">¥{{ selectedOrder.depositPaid }}</span>
              </div>
              <div class="refund-row refundable" v-if="isDepositRefundable(selectedOrder.status)">
                <span class="label">可退定金:</span>
                <span class="value refundable-value">¥{{ selectedOrder.depositPaid }}</span>
              </div>
              <div class="refund-row non-refundable" v-else>
                <span class="label">定金状态:</span>
                <span class="value non-refundable-value">不予退还 (尾款超时未支付)</span>
              </div>
            </div>
          </div>

          <div class="refund-row balance-section" v-if="selectedOrder.balancePaid > 0">
            <div class="section-header">
              <i class="el-icon-wallet"></i>
              <span>尾款部分</span>
            </div>
            <div class="section-content">
              <div class="refund-row">
                <span class="label">已支付尾款:</span>
                <span class="value">¥{{ selectedOrder.balancePaid }}</span>
              </div>
              <div class="refund-row refundable">
                <span class="label">可退尾款:</span>
                <span class="value refundable-value">¥{{ selectedOrder.balancePaid }}</span>
              </div>
            </div>
          </div>
        </div>

        <el-divider></el-divider>

        <div class="refund-summary">
          <div class="refund-total">
            <span class="label">可退款总额:</span>
            <span class="value total-amount">¥{{ refundAmount.toFixed(2) }}</span>
          </div>
          <p class="refund-note" v-if="getRefundNote(selectedOrder.status)">
            <i class="el-icon-info"></i>
            {{ getRefundNote(selectedOrder.status) }}
          </p>
        </div>

        <el-form class="refund-reason-form">
          <el-form-item label="退款原因">
            <el-input v-model="refundReason" type="textarea" :rows="3" placeholder="请输入退款原因"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer">
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmRefund" :loading="refunding">确认退款</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'OrderList',
  data() {
    return {
      orders: [],
      allProducts: [],
      activeTab: 'all',
      balanceDialogVisible: false,
      refundDialogVisible: false,
      selectedOrder: null,
      paying: false,
      refunding: false,
      refundAmount: 0,
      refundReason: '',
      currentUser: null,
      refreshTimer: null
    }
  },
  computed: {
    filteredOrders() {
      if (this.activeTab === 'all') return this.orders
      if (this.activeTab === 'deposit') {
        return this.orders.filter(o => o.status === 'DEPOSIT_PAID')
      }
      if (this.activeTab === 'balance') {
        return this.orders.filter(o => o.status === 'BALANCE_PERIOD')
      }
      if (this.activeTab === 'pending') {
        return this.orders.filter(o => o.status === 'BALANCE_PAID' || o.status === 'SHIPPED')
      }
      if (this.activeTab === 'completed') {
        return this.orders.filter(o => o.status === 'COMPLETED')
      }
      if (this.activeTab === 'cancelled') {
        return this.orders.filter(o =>
          o.status === 'CANCELLED' || o.status === 'DEPOSIT_TIMEOUT' || o.status === 'BALANCE_TIMEOUT'
        )
      }
      return this.orders
    }
  },
  created() {
    this.loadUser()
    this.loadOrders()
    this.loadProducts()
    this.startAutoRefresh()
  },
  beforeDestroy() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
    }
  },
  methods: {
    loadUser() {
      const savedUser = localStorage.getItem('currentUser')
      if (savedUser) {
        this.currentUser = JSON.parse(savedUser)
      }
    },
    startAutoRefresh() {
      this.refreshTimer = setInterval(() => {
        this.loadOrders()
      }, 5000)
    },
    loadOrders() {
      if (!this.currentUser) return
      axios.get(`http://localhost:8004/api/orders/user/${this.currentUser.id}`)
        .then(res => {
          if (res.code === 200) {
            this.orders = res.data
          }
        })
        .catch(() => {})
    },
    loadProducts() {
      axios.get('http://localhost:8004/api/products')
        .then(res => {
          if (res.code === 200) {
            this.allProducts = res.data
          }
        })
        .catch(() => {})
    },
    endPreorderActivity(product) {
      this.$confirm(
        `确定要提前结束「${product.name}」的预售活动吗？\n\n提前结束后：\n1. 已支付定金的订单将进入24小时尾款支付期\n2. 未支付定金的订单将被取消\n3. 活动结束后无法恢复`,
        '提示',
        {
          confirmButtonText: '确定结束',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        axios.post(`http://localhost:8004/api/orders/end-activity/${product.id}`)
          .then(res => {
            if (res.code === 200) {
              this.$message.success('预售活动已提前结束')
              this.loadProducts()
              this.loadOrders()
            } else {
              this.$message.error(res.message)
            }
          })
          .catch(err => {
            this.$message.error(err.message || '操作失败')
          })
      }).catch(() => {})
    },
    handleTabClick() {
    },
    getStatusType(status) {
      const typeMap = {
        'DEPOSIT_PENDING': 'info',
        'DEPOSIT_PAID': 'warning',
        'BALANCE_PERIOD': 'warning',
        'BALANCE_PAID': 'primary',
        'SHIPPED': 'primary',
        'COMPLETED': 'success',
        'CANCELLED': 'danger',
        'DEPOSIT_TIMEOUT': 'danger',
        'BALANCE_TIMEOUT': 'danger'
      }
      return typeMap[status] || 'info'
    },
    getStatusText(status) {
      const textMap = {
        'DEPOSIT_PENDING': '待支付定金',
        'DEPOSIT_PAID': '定金期(已付定金)',
        'BALANCE_PERIOD': '尾款期(待付尾款)',
        'BALANCE_PAID': '待发货',
        'SHIPPED': '已发货',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消',
        'DEPOSIT_TIMEOUT': '定金超时',
        'BALANCE_TIMEOUT': '尾款超时'
      }
      return textMap[status] || status
    },
    getOrderStageType(status) {
      if (status === 'DEPOSIT_PAID') return 'warning'
      if (status === 'BALANCE_PERIOD') return 'warning'
      if (status === 'BALANCE_PAID' || status === 'SHIPPED') return 'primary'
      if (status === 'COMPLETED') return 'success'
      if (status === 'BALANCE_TIMEOUT') return 'danger'
      return 'info'
    },
    getOrderStageText(status) {
      const stageMap = {
        'DEPOSIT_PAID': '定金阶段 (已支付定金，未到尾款期)',
        'BALANCE_PERIOD': '尾款阶段 (尾款支付中)',
        'BALANCE_PAID': '已完成支付 (待发货)',
        'SHIPPED': '已发货',
        'COMPLETED': '已完成',
        'BALANCE_TIMEOUT': '尾款超时 (定金不予退还)'
      }
      return stageMap[status] || status
    },
    isDepositRefundable(status) {
      return status === 'DEPOSIT_PAID' || 
             status === 'BALANCE_PERIOD' || 
             status === 'BALANCE_PAID' || 
             status === 'SHIPPED' || 
             status === 'COMPLETED'
    },
    getRefundNote(status) {
      const noteMap = {
        'DEPOSIT_PAID': '定金期内申请退款，定金全额退还',
        'BALANCE_PERIOD': '尾款期内申请退款，定金全额退还',
        'BALANCE_PAID': '已支付全款，申请退款全额退还（含定金和尾款）',
        'SHIPPED': '已发货状态申请退款，全额退还（需退回商品）',
        'COMPLETED': '已完成订单申请退款，全额退还（需退回商品）',
        'BALANCE_TIMEOUT': '尾款超时未支付，定金不予退还'
      }
      return noteMap[status] || ''
    },
    payBalance(order) {
      this.selectedOrder = order
      this.balanceDialogVisible = true
    },
    confirmPayBalance() {
      if (!this.selectedOrder) return
      this.paying = true
      axios.post(`http://localhost:8004/api/orders/${this.selectedOrder.orderNo}/pay-balance`)
        .then(res => {
          if (res.code === 200) {
            this.$message.success('尾款支付成功')
            this.balanceDialogVisible = false
            this.loadOrders()
            this.updateUserBalance()
          } else {
            this.$message.error(res.message)
          }
        })
        .catch(err => {
          this.$message.error(err.message || '支付失败')
        })
        .finally(() => {
          this.paying = false
        })
    },
    cancelOrder(order) {
      this.$confirm('确定要取消订单吗？定金将原路退回。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.post(`http://localhost:8004/api/orders/${order.orderNo}/cancel`, { reason: '用户取消' })
          .then(res => {
            if (res.code === 200) {
              this.$message.success('订单已取消')
              this.loadOrders()
              this.updateUserBalance()
            } else {
              this.$message.error(res.message)
            }
          })
          .catch(err => {
            this.$message.error(err.message || '取消失败')
          })
      }).catch(() => {})
    },
    applyRefund(order) {
      this.selectedOrder = order
      this.refundReason = ''
      axios.get(`http://localhost:8004/api/orders/${order.orderNo}/refund-amount`)
        .then(res => {
          if (res.code === 200) {
            this.refundAmount = res.data
            this.refundDialogVisible = true
          } else {
            this.$message.error(res.message)
          }
        })
    },
    confirmRefund() {
      if (!this.selectedOrder) return
      this.refunding = true
      axios.post(`http://localhost:8004/api/orders/${this.selectedOrder.orderNo}/refund`, {
        reason: this.refundReason
      })
        .then(res => {
          if (res.code === 200) {
            this.$message.success('退款申请已提交')
            this.refundDialogVisible = false
            this.loadOrders()
            this.updateUserBalance()
          } else {
            this.$message.error(res.message)
          }
        })
        .catch(err => {
          this.$message.error(err.message || '申请失败')
        })
        .finally(() => {
          this.refunding = false
        })
    },
    confirmReceive(order) {
      this.$confirm('确认已收到商品吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.post(`http://localhost:8004/api/orders/${order.orderNo}/complete`)
          .then(res => {
            if (res.code === 200) {
              this.$message.success('确认收货成功')
              this.loadOrders()
            } else {
              this.$message.error(res.message)
            }
          })
          .catch(err => {
            this.$message.error(err.message || '操作失败')
          })
      }).catch(() => {})
    },
    updateUserBalance() {
      if (!this.currentUser) return
      axios.get(`http://localhost:8004/api/users/${this.currentUser.id}`)
        .then(res => {
          if (res.code === 200) {
            this.currentUser = res.data
            localStorage.setItem('currentUser', JSON.stringify(this.currentUser))
          }
        })
    },
    getCountdown(endTimeStr) {
      if (!endTimeStr) return ''
      const endTime = new Date(endTimeStr).getTime()
      const now = new Date().getTime()
      const diff = endTime - now
      
      if (diff <= 0) return '已超时'
      
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))
      const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
      const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
      const seconds = Math.floor((diff % (1000 * 60)) / 1000)
      
      if (days > 0) {
        return `${days}天${hours}小时${minutes}分钟`
      } else if (hours > 0) {
        return `${hours}小时${minutes}分钟`
      } else if (minutes > 0) {
        return `${minutes}分钟${seconds}秒`
      } else {
        return `${seconds}秒`
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.order-list {
  padding: 20px;
}

.order-list h2 {
  margin-bottom: 20px;
  color: #303133;
}

.order-card {
  margin-bottom: 20px;
}

.order-header {
  display: flex;
  align-items: center;
  gap: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 15px;
}

.order-no {
  font-weight: bold;
  color: #303133;
}

.order-time {
  margin-left: auto;
  color: #909399;
  font-size: 13px;
}

.order-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.product-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.product-info p {
  margin: 4px 0;
  color: #606266;
  font-size: 13px;
}

.order-total {
  font-size: 14px !important;
  font-weight: bold;
}

.price {
  color: #f56c6c;
  font-size: 18px;
}

.payment-info {
  min-width: 150px;
  text-align: right;
}

.payment-info p {
  margin: 4px 0;
  font-size: 13px;
  color: #606266;
}

.deposit {
  color: #e6a23c;
  font-weight: bold;
}

.balance {
  color: #409EFF;
  font-weight: bold;
}

.pending-balance {
  color: #f56c6c;
  font-weight: bold;
}

.refund {
  color: #67c23a;
  font-weight: bold;
}

.cancel-reason {
  color: #f56c6c;
  font-size: 12px !important;
}

.order-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 120px;
}

.order-footer {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #ebeef5;
  color: #909399;
  font-size: 12px;
}

.balance-form p {
  margin: 10px 0;
}

.pay-amount {
  font-size: 16px;
  font-weight: bold;
}

.pay-amount span {
  color: #f56c6c;
  font-size: 24px;
}

.refund-form {
  padding: 10px 0;
}

.refund-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 10px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.refund-product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.refund-product-info h4 {
  margin: 0 0 5px 0;
  font-size: 15px;
  color: #303133;
}

.refund-product-info p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.divider-title {
  font-weight: bold;
  color: #303133;
  font-size: 14px;
}

.order-stage-badge {
  text-align: center;
  margin: 10px 0 20px 0;
}

.refund-calculation {
  background: #fafafa;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 10px;
}

.refund-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.refund-row:last-child {
  margin-bottom: 0;
}

.refund-row .label {
  color: #606266;
  font-size: 14px;
}

.refund-row .value {
  color: #303133;
  font-weight: 500;
  font-size: 14px;
}

.deposit-section,
.balance-section {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 12px;
  margin: 15px 0;
}

.deposit-section {
  border-left: 4px solid #e6a23c;
}

.balance-section {
  border-left: 4px solid #409EFF;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #ebeef5;
}

.section-header i {
  font-size: 18px;
}

.deposit-section .section-header {
  color: #e6a23c;
}

.balance-section .section-header {
  color: #409EFF;
}

.section-header span {
  font-weight: bold;
  font-size: 14px;
}

.section-content .refund-row {
  margin-bottom: 8px;
  padding-left: 10px;
}

.refundable .refundable-value {
  color: #67c23a;
  font-weight: bold;
}

.non-refundable .non-refundable-value {
  color: #f56c6c;
  font-weight: bold;
}

.refund-summary {
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
}

.refund-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.refund-total .label {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.total-amount {
  color: #67c23a;
  font-size: 28px;
  font-weight: bold;
}

.refund-note {
  margin: 10px 0 0 0;
  color: #909399;
  font-size: 12px;
}

.refund-note i {
  margin-right: 5px;
  color: #409EFF;
}

.refund-reason-form {
  margin-top: 20px;
}

.empty {
  padding: 50px 0;
}

.activity-collapse {
  margin-bottom: 20px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.activity-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.activity-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.activity-name {
  font-weight: 500;
  color: #303133;
}

.activity-stock {
  color: #909399;
  font-size: 13px;
}

.balance-period-card {
  border: 2px solid #f56c6c;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.2);
}

.balance-notice {
  background: linear-gradient(135deg, #fff6f6 0%, #ffecec 100%);
  border: 1px solid #ffcdd2;
  border-radius: 4px;
  padding: 12px 15px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.balance-notice i {
  color: #f56c6c;
  font-size: 18px;
}

.balance-notice span {
  color: #f56c6c;
  font-size: 13px;
}

.balance-notice .countdown {
  background: #f56c6c;
  color: #fff !important;
  padding: 2px 10px;
  border-radius: 12px;
  font-weight: bold;
  margin-left: auto;
}

.balance-payment-box {
  background: #fff6f6;
  border: 1px solid #ffcdd2;
  border-radius: 4px;
  padding: 12px;
  margin: 8px 0;
  text-align: center;
}

.payable-label {
  font-size: 13px;
  color: #909399;
  margin: 0 0 5px 0;
}

.payable-amount {
  font-size: 28px;
  font-weight: bold;
  color: #f56c6c;
  margin: 0 0 5px 0;
}

.payment-detail {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

.deposit-period-notice {
  background: #fdf6ec;
  border: 1px solid #faecd8;
  border-radius: 4px;
  padding: 8px 12px;
  margin: 8px 0;
  color: #e6a23c !important;
  font-size: 12px;
}

.deposit-period-notice i {
  margin-right: 5px;
}

.pay-balance-btn {
  width: 100%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(245, 108, 108, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0);
  }
}
</style>
