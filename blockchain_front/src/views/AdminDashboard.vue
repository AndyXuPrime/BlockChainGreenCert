<template>
  <div class="dashboard-container">
    <div class="header admin-header">
      <div>
        <h2 style="margin: 0 0 5px 0;">🛡️ 监管机构控制台</h2>
        <small style="color: #666;">
          机构名称: <strong>{{ currentUser.companyName }}</strong> | ID: {{ currentUser.id }}<br>
          区块链地址: {{ currentUser.chainAddress }}
        </small>
      </div>
      <button class="btn-danger btn-small" @click="logout">退出登录</button>
    </div>

    <div class="card admin-view">
      <h2>1. 核发绿证 (上链存证)</h2>
      <input v-model="issueForm.certId" placeholder="输入证书编号 (如: GC-2024-001)">
      <input v-model="issueForm.ownerId" type="number" placeholder="接收企业在系统中的 ID (如: 2)">
      <input v-model="issueForm.energyType" placeholder="能源类型 (如: 光伏)">
      <input v-model="issueForm.amount" type="number" placeholder="电量 (MWh)">
      <button class="btn-red" @click="issueCert">一键上链核发</button>
    </div>

    <div class="card admin-view">
      <h2>2. 异常纠错：作废绿证</h2>
      <input v-model="revokeCertId" placeholder="输入要作废的证书编号">
      <button class="btn-danger" @click="revokeCert">强制逻辑作废</button>
    </div>

    <!-- 监管机构也需要查验功能 -->
    <div class="card">
      <h2>3. 绿证链上防篡改核验</h2>
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
  name: 'AdminDashboard',
  data() {
    return {
      currentUser: JSON.parse(localStorage.getItem('currentUser') || '{}'),
      issueForm: { certId: '', ownerId: '', energyType: '', amount: '' },
      revokeCertId: '',
      verifyCertId: '',
      verifyResult: null
    }
  },
  methods: {
    logout() {
      localStorage.removeItem('currentUser');
      this.$router.push('/login');
    },
    async issueCert() {
      if(!this.issueForm.certId || !this.issueForm.ownerId) return alert("请填写完整");
      try {
        const msg = await request.post('/api/cert/issue', this.issueForm);
        alert("✅ " + msg);
        this.issueForm = { certId: '', ownerId: '', energyType: '', amount: '' }; // 清空表单
      } catch (err) { alert("❌ " + err.message); }
    },
    async revokeCert() {
      if(!this.revokeCertId) return alert("请输入编号");
      if(!confirm("确定作废吗？此操作将记录在区块链上且不可逆！")) return;
      try {
        const msg = await request.post('/api/cert/revoke?certId=' + this.revokeCertId);
        alert("🛑 " + msg);
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
/* 样式复用，详见下方统一说明 */
@import '../assets/dashboard.css';
.admin-header { border-left: 5px solid #f44336; }
.admin-view { border-left: 5px solid #f44336; }
.btn-red { background-color: #f44336; }
.btn-red:hover { background-color: #d32f2f; }
</style>