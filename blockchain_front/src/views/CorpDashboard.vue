<template>
  <el-container class="layout-container">
    <!-- 左侧侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span style="font-size: 20px;">🏢 企业控制台</span>
      </div>
      <el-menu :default-active="activeMenu" background-color="#436193" text-color="#fff" active-text-color="#a8f49d" @select="handleSelect">
        <el-menu-item index="dashboard"><i class="el-icon-s-promotion"></i>业务操作台</el-menu-item>
        <el-menu-item index="mycerts"><i class="el-icon-wallet"></i>我的绿证资产</el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主区域 -->
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <strong>{{ currentUser.companyName }}</strong> (ID: {{ currentUser.id }})
          <span class="address-tag">{{ currentUser.chainAddress }}</span>
        </div>
        <el-button type="danger" size="small" @click="logout">退出系统</el-button>
      </el-header>

      <el-main class="main-content">

        <!-- 视图 1: 业务操作台 -->
        <div v-show="activeMenu === 'dashboard'">
          <el-row :gutter="20">
            <!-- 流转模块 -->
            <el-col :span="12">
              <el-card shadow="hover" class="box-card corp-card">
                <template #header><b>1. 跨域流转交易 (资产转移)</b></template>
                <el-input v-model="transferForm.certId" placeholder="要流转的证书编号 (如: GC-001)" class="mb-10"></el-input>
                <el-input v-model="transferForm.toUserId" type="number" placeholder="买家企业在系统中的 ID (如: 3)" class="mb-10"></el-input>
                <el-button type="primary" class="btn-green w-100" @click="transferCert">发起流转交易 (自动签名)</el-button>
              </el-card>
            </el-col>

            <!-- 核验模块 -->
            <el-col :span="12">
              <el-card shadow="hover" class="box-card">
                <template #header><b>2. 绿证链上防篡改核验 (买家查验)</b></template>
                <el-input v-model="verifyCertId" placeholder="输入要查验的证书编号" class="mb-10"></el-input>
                <el-button type="primary" class="w-100" @click="verifyCert">交叉比对 (MySQL vs 区块链)</el-button>
                <div v-if="verifyResult" :class="['result-box', verifyResult.success ? 'success-box' : 'error-box']">
                  {{ verifyResult.msg }}
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 视图 2: 我的绿证 (新增) -->
        <div v-show="activeMenu === 'mycerts'">
          <el-card shadow="hover">
            <template #header><b>我的绿证资产库</b></template>
            <el-table :data="myCertList" border style="width: 100%">
              <el-table-column prop="certId" label="证书编号" width="150"></el-table-column>
              <el-table-column prop="energyType" label="能源类型" width="120"></el-table-column>
              <el-table-column prop="amount" label="电量(MWh)" width="120"></el-table-column>
              <el-table-column prop="issueTime" label="核发时间" :formatter="formatDate"></el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                    {{ scope.row.status === 1 ? '有效' : '已作废' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="txHash" label="存证哈希" show-overflow-tooltip></el-table-column>
            </el-table>
          </el-card>
        </div>

      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import request from '@/utils/request';

export default {
  name: 'CorpDashboard',
  data() {
    return {
      activeMenu: 'dashboard',
      currentUser: JSON.parse(localStorage.getItem('currentUser') || '{}'),
      transferForm: { certId: '', toUserId: '' },
      verifyCertId: '',
      verifyResult: null,
      myCertList: [] // 我的绿证列表
    }
  },
  created() {
    this.fetchMyCerts();
  },
  methods: {
    handleSelect(index) {
      this.activeMenu = index;
      if (index === 'mycerts') this.fetchMyCerts();
    },
    logout() {
      localStorage.removeItem('currentUser');
      this.$router.push('/login');
    },
    formatDate(row, column, cellValue) {
      if (!cellValue) return '';
      return new Date(cellValue).toLocaleString();
    },
    // --- 新增：获取我的绿证 ---
    async fetchMyCerts() {
      try {
        this.myCertList = await request.get('/api/cert/myCerts/' + this.currentUser.id);
      } catch (e) { console.error(e); }
    },
    // --- 原有逻辑 ---
    async transferCert() {
      if(!this.transferForm.certId || !this.transferForm.toUserId) return alert("请填写完整");
      try {
        const msg = await request.post('/api/cert/transfer', {
          certId: this.transferForm.certId,
          fromUserId: this.currentUser.id,
          toUserId: parseInt(this.transferForm.toUserId)
        });
        alert("🚀 " + msg);
        this.transferForm = { certId: '', toUserId: '' };
        this.fetchMyCerts(); // 刷新列表
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
/* 与 AdminDashboard 相同的样式 */
.layout-container { height: 100vh; }
.sidebar { background-color: #436193; color: white; }
.logo { height: 60px; line-height: 60px; text-align: center; font-weight: bold; border-bottom: 1px solid rgba(255,255,255,0.1); }
.header { background-color: #fff; border-bottom: 1px solid #e6e6e6; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; }
.address-tag { background: #f0f9eb; color: #2ac079; padding: 2px 8px; border-radius: 4px; font-size: 12px; margin-left: 10px; border: 1px solid #e1f3d8;}
.main-content { background-color: #f0f2f5; padding: 20px; }
.mb-10 { margin-bottom: 10px; }
.w-100 { width: 100%; }
.btn-green { background-color: #2ac079; border-color: #2ac079; }
.btn-green:hover { background-color: #23a065; border-color: #23a065; }
.corp-card { border-top: 4px solid #2ac079; }
.result-box { margin-top: 15px; padding: 15px; border-radius: 4px; font-weight: bold; }
.success-box { background: #e8f5e9; border: 1px solid #c8e6c9; color: #2e7d32; }
.error-box { background: #ffebee; border: 1px solid #ffcdd2; color: #c62828; }
</style>