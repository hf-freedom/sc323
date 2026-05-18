const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 3004,
    host: '0.0.0.0',
    client: {
      overlay: false
    }
  },
  lintOnSave: false
})
