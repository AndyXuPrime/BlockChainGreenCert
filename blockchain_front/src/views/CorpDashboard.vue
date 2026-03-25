<template>
  <div class="dashboard-container">
    <div class="header corp-header">
      <div>
        <h2 style="margin: 0 0 5px 0;">🏢 {{ currentUser.companyName }} 控制台</h2>
        <small style="color: #666;">
          角色: <strong>企业端</strong> | ID: {{ currentUser.id }}<br>
          区块链地址: {{ currentUser.chainAddress }}
        </small>
      </div>
      <button class="btn-danger btn-small" @click="logout">退出登录</button>
    </div>

    <div class="card corp-view">
      <h2>1. 跨域流转交易 (资产转移)</h2>
      <input v-model="transferForm.certId" placeholder="要流转的证书编号 (如: GC-2024-001)">
      <input v-model="transferForm.toUserId" type="number" placeholder="买家企业在系统中的 ID (如: 3)">
      <button class="btn-green" @click="transferCert">发起流转交易 (系统自动使用您的私钥签名)</button>
    </div>

    <div class="card">
      <h2>2. 绿证链上防篡改核验 (买家查验)</h2>
      <input v-model="verifyCertId" placeholder="输入要查验的证书编号">
      <button class="btn-blue" @click="verifyCert">交叉比对 (MySQL vs 区块链)</button>
      <div v-if="verifyResult" :class="['result-box', verifyResult.success ? '' : 'error-box']">
        {{ verifyResult.msg }}
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';

export default {
  name: 'CorpDashboard',
  data() {
    return {
      currentUser: JSON.parse(localStorage.getItem('currentUser') || '{}'),
      transferForm: { certId: '', toUserId: '' },
      verifyCertId: '',
      verifyResult: null
    }
  },
  methods: {
    logout() {
      localStorage.removeItem('currentUser');
      this.$router.push('/login');
    },
    async transferCert() {
      if(!this.transferForm.certId || !this.transferForm.toUserId) return alert("请填写完整");
      try {
        const msg = await request.post('/api/cert/transfer', {
          certId: this.transferForm.certId,
          fromUserId: this.currentUser.id, // 后端校验权限用
          toUserId: parseInt(this.transferForm.toUserId)
        });
        alert("🚀 " + msg);
        this.transferForm = { certId: '', toUserId: '' }; // 清空表单
      } catch (err) { alert("❌ " + err.message); }
    },
    async verifyCert() {
      if(!this.verifyCertId) return alert("请输入编号");
      try {
        const msg = await request.get('/api/cert/verify/' + this.verifyCertId);
        this.verifyResult = { success: true, msg: msg };
      } catch (err) {
        this.verifyResult = { success: false, msg: err.message };
      }
    }
  }
}
</script>

<style scoped>
@import '../assets/dashboard.css';
.corp-header { border-left: 5px solid #4CAF50; }
.corp-view { border-left: 5px solid #4CAF50; }
.btn-green { background-color: #4CAF50; }
.btn-green:hover { background-color: #45a049; }
</style>