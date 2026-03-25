<template>
  <div class="login-container">
    <h1 style="text-align: center; color: #333;">🌱 绿证核发与流转系统</h1>

    <div class="card">
      <div class="nav-tabs">
        <span @click="authMode = 'login'" :class="{active: authMode === 'login'}">系统登录</span>
        <span @click="authMode = 'register'" :class="{active: authMode === 'register'}">企业注册</span>
      </div>

      <!-- 登录表单 -->
      <div v-if="authMode === 'login'">
        <input v-model="loginForm.username" placeholder="请输入用户名 (如: admin 或 corp_a)">
        <input v-model="loginForm.password" type="password" placeholder="请输入密码">
        <button @click="handleLogin">登 录</button>
      </div>

      <!-- 注册表单 -->
      <div v-if="authMode === 'register'">
        <input v-model="regForm.username" placeholder="设置登录用户名">
        <input v-model="regForm.password" type="password" placeholder="设置登录密码">
        <input v-model="regForm.companyName" placeholder="企业真实名称 (如: 深圳新能源)">
        <button @click="handleRegister">注 册 并 上 链 开 户</button>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';

export default {
  name: 'Login',
  data() {
    return {
      authMode: 'login',
      loginForm: { username: '', password: '' },
      regForm: { username: '', password: '', companyName: '' }
    }
  },
  methods: {
    async handleLogin() {
      if(!this.loginForm.username || !this.loginForm.password) return alert("请输入账号密码");
      try {
        const user = await request.post('/auth/login', this.loginForm);
        localStorage.setItem('currentUser', JSON.stringify(user));

        // 【核心修改】根据角色跳转不同页面
        if (user.role === 'ADMIN') {
          this.$router.push('/admin');
        } else {
          this.$router.push('/corp');
        }
      } catch (err) {
        alert("❌ 登录失败: " + err.message);
      }
    },
    async handleRegister() {
      if(!this.regForm.username || !this.regForm.password || !this.regForm.companyName) return alert("请填写完整信息");
      try {
        await request.post('/auth/register', this.regForm);
        alert("🎉 注册成功！区块链账户已生成，请登录。");
        this.authMode = 'login';
      } catch (err) {
        alert("❌ 注册失败: " + err.message);
      }
    }
  }
}
</script>

<style scoped>
.login-container { max-width: 400px; margin: 50px auto; }
.card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.nav-tabs { margin-bottom: 20px; border-bottom: 2px solid #eee; padding-bottom: 10px;}
.nav-tabs span { cursor: pointer; margin-right: 20px; color: #666; font-size: 16px; font-weight: bold; }
.nav-tabs span.active { color: #2196F3; border-bottom: 2px solid #2196F3; padding-bottom: 10px; }
input { width: 100%; padding: 12px; margin: 10px 0; box-sizing: border-box; border: 1px solid #ccc; border-radius: 4px; }
button { width: 100%; padding: 12px; background-color: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; margin-top: 10px;}
button:hover { background-color: #45a049; }
</style>