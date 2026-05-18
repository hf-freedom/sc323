<template>
  <div id="app">
    <el-container>
      <el-header class="header">
        <div class="logo">预售尾款系统</div>
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          router
          class="nav-menu"
        >
          <el-menu-item index="/">商品列表</el-menu-item>
          <el-menu-item index="/orders">我的订单</el-menu-item>
        </el-menu>
        <div class="user-info">
          <span>当前用户: {{ currentUser.nickname }}</span>
          <span class="balance">余额: ¥{{ currentUser.balance }}</span>
          <el-button size="mini" type="primary" @click="showRecharge = true">充值</el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>

    <el-dialog title="账户充值" :visible.sync="showRecharge" width="400px">
      <el-form>
        <el-form-item label="充值金额">
          <el-input-number v-model="rechargeAmount" :min="1" :max="100000" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showRecharge = false">取消</el-button>
        <el-button type="primary" @click="handleRecharge">确认充值</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'App',
  data() {
    return {
      currentUser: {
        id: 1,
        username: 'user001',
        nickname: '测试用户001',
        balance: '10000.00'
      },
      showRecharge: false,
      rechargeAmount: 100
    }
  },
  computed: {
    activeMenu() {
      return this.$route.path
    }
  },
  created() {
    this.loadUser()
  },
  methods: {
    loadUser() {
      const savedUser = localStorage.getItem('currentUser')
      if (savedUser) {
        this.currentUser = JSON.parse(savedUser)
      } else {
        this.login()
      }
    },
    login() {
      axios.post('http://localhost:8004/api/users/login', { username: 'user001' })
        .then(res => {
          if (res.data.code === 200) {
            this.currentUser = res.data.data
            localStorage.setItem('currentUser', JSON.stringify(this.currentUser))
            this.$emit('user-updated', this.currentUser)
          }
        })
        .catch(() => {
          localStorage.setItem('currentUser', JSON.stringify(this.currentUser))
        })
    },
    handleRecharge() {
      axios.post(`http://localhost:8004/api/users/${this.currentUser.id}/recharge`, {
        amount: this.rechargeAmount
      }).then(res => {
        if (res.data.code === 200) {
          this.currentUser = res.data.data
          localStorage.setItem('currentUser', JSON.stringify(this.currentUser))
          this.showRecharge = false
          this.$message.success('充值成功')
          this.$emit('user-updated', this.currentUser)
        } else {
          this.$message.error(res.data.message)
        }
      }).catch(err => {
        this.$message.error('充值失败: ' + err.message)
      })
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.nav-menu {
  border-bottom: none;
  flex: 1;
  margin-left: 40px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 14px;
}

.balance {
  color: #f56c6c;
  font-weight: bold;
}

.main-content {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
