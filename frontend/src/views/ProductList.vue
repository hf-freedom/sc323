<template>
  <div class="product-list">
    <h2>预售商品列表</h2>
    <div class="product-grid">
      <el-card
        v-for="product in products"
        :key="product.id"
        class="product-card"
        :body-style="{ padding: '0px' }"
      >
        <img :src="product.image" class="product-image" />
        <div class="product-info">
          <h3>{{ product.name }}</h3>
          <p class="description">{{ product.description }}</p>
          <div class="price-section">
            <span class="original-price">¥{{ product.originalPrice }}</span>
            <span class="preorder-price">¥{{ product.preorderPrice }}</span>
          </div>
          <div class="deposit-section">
            <el-tag type="warning">定金: ¥{{ product.deposit }}</el-tag>
            <span class="stock-info">库存: {{ product.totalStock - product.lockedStock }}/{{ product.totalStock }}</span>
          </div>
          <div class="time-section">
            <p><i class="el-icon-time"></i> 定金支付: {{ formatDate(product.depositStartTime) }} ~ {{ formatDate(product.depositEndTime) }}</p>
            <p><i class="el-icon-time"></i> 尾款支付: {{ formatDate(product.balanceStartTime) }} ~ {{ formatDate(product.balanceEndTime) }}</p>
          </div>
          <div class="rule-section">
            <el-tag size="mini" type="info">{{ product.depositRule }}</el-tag>
          </div>
          <div class="action-section">
            <el-button
              type="primary"
              :disabled="!canBuy(product)"
              @click="openDepositDialog(product)"
            >
              {{ canBuy(product) ? '付定金预售' : getButtonText(product) }}
            </el-button>
            <el-button
              v-if="product.active"
              type="danger"
              size="mini"
              @click.stop="endPreorderActivity(product)"
              style="margin-top: 8px;"
            >
              提前结束活动
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <el-dialog title="支付定金" :visible.sync="depositDialogVisible" width="500px">
      <div v-if="selectedProduct" class="deposit-form">
        <div class="product-summary">
          <img :src="selectedProduct.image" class="summary-image" />
          <div class="summary-info">
            <h3>{{ selectedProduct.name }}</h3>
            <p>预售价: <span class="price">¥{{ selectedProduct.preorderPrice }}</span></p>
            <p>定金: <span class="deposit">¥{{ selectedProduct.deposit }}</span></p>
            <p>尾款: <span class="balance">¥{{ selectedProduct.preorderPrice - selectedProduct.deposit }}</span></p>
          </div>
        </div>
        <el-form>
          <el-form-item label="购买数量">
            <el-input-number
              v-model="buyQuantity"
              :min="1"
              :max="selectedProduct.totalStock - selectedProduct.lockedStock"
            />
          </el-form-item>
          <el-form-item label="应付定金">
            <span class="total-deposit">¥{{ (selectedProduct.deposit * buyQuantity).toFixed(2) }}</span>
          </el-form-item>
        </el-form>
      </div>
      <span slot="footer">
        <el-button @click="depositDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="payDeposit" :loading="paying">确认支付定金</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'ProductList',
  data() {
    return {
      products: [],
      depositDialogVisible: false,
      selectedProduct: null,
      buyQuantity: 1,
      paying: false,
      currentUser: null
    }
  },
  created() {
    this.loadUser()
    this.loadProducts()
  },
  methods: {
    loadUser() {
      const savedUser = localStorage.getItem('currentUser')
      if (savedUser) {
        this.currentUser = JSON.parse(savedUser)
      }
    },
    loadProducts() {
      axios.get('http://localhost:8004/api/products?active=true')
        .then(res => {
          if (res.code === 200) {
            this.products = res.data
          }
        })
        .catch(() => {
          this.$message.error('加载商品列表失败')
        })
    },
    canBuy(product) {
      const now = new Date()
      const depositStart = new Date(product.depositStartTime)
      const depositEnd = new Date(product.depositEndTime)
      const availableStock = product.totalStock - product.lockedStock
      return product.active && availableStock > 0 && now >= depositStart && now <= depositEnd
    },
    getButtonText(product) {
      const now = new Date()
      const depositStart = new Date(product.depositStartTime)
      const depositEnd = new Date(product.depositEndTime)
      const availableStock = product.totalStock - product.lockedStock

      if (!product.active) return '活动已结束'
      if (availableStock <= 0) return '已售罄'
      if (now < depositStart) return '预售未开始'
      if (now > depositEnd) return '定金已截止'
      return '付定金预售'
    },
    openDepositDialog(product) {
      this.selectedProduct = product
      this.buyQuantity = 1
      this.depositDialogVisible = true
    },
    payDeposit() {
      if (!this.currentUser) {
        this.$message.error('请先登录')
        return
      }

      this.paying = true
      axios.post('http://localhost:8004/api/orders/deposit', {
        productId: this.selectedProduct.id,
        userId: this.currentUser.id,
        quantity: this.buyQuantity
      }).then(res => {
        if (res.code === 200) {
          const order = res.data
          return axios.post(`http://localhost:8004/api/orders/${order.orderNo}/pay-deposit`)
        } else {
          throw new Error(res.message)
        }
      }).then(res => {
        if (res.code === 200) {
          this.$message.success('定金支付成功')
          this.depositDialogVisible = false
          this.loadProducts()
          this.updateUserBalance()
          setTimeout(() => {
            this.$router.push('/orders')
          }, 1000)
        } else {
          this.$message.error(res.message)
        }
      }).catch(err => {
        this.$message.error(err.message || '支付失败')
      }).finally(() => {
        this.paying = false
      })
    },
    updateUserBalance() {
      axios.get(`http://localhost:8004/api/users/${this.currentUser.id}`)
        .then(res => {
          if (res.code === 200) {
            this.currentUser = res.data
            localStorage.setItem('currentUser', JSON.stringify(this.currentUser))
          }
        })
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
            } else {
              this.$message.error(res.message)
            }
          })
          .catch(err => {
            this.$message.error(err.message || '操作失败')
          })
      }).catch(() => {})
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.product-list {
  padding: 20px;
}

.product-list h2 {
  margin-bottom: 20px;
  color: #303133;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.product-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.15);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
  height: 40px;
  overflow: hidden;
}

.description {
  color: #909399;
  font-size: 13px;
  height: 36px;
  overflow: hidden;
  margin-bottom: 10px;
}

.price-section {
  margin-bottom: 10px;
}

.original-price {
  text-decoration: line-through;
  color: #909399;
  font-size: 13px;
  margin-right: 10px;
}

.preorder-price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.deposit-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.stock-info {
  font-size: 13px;
  color: #606266;
}

.time-section {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
  line-height: 1.8;
}

.rule-section {
  margin-bottom: 15px;
}

.action-section {
  text-align: center;
}

.deposit-form {
  padding: 20px 0;
}

.product-summary {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.summary-image {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.summary-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
}

.summary-info p {
  margin: 5px 0;
  color: #606266;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.deposit {
  color: #e6a23c;
  font-weight: bold;
}

.balance {
  color: #409EFF;
  font-weight: bold;
}

.total-deposit {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}
</style>
