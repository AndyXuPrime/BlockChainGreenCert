<template>
  <el-container class="layout-container">
    <!-- 左侧侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span style="font-size: 20px;">🛡️ 监管控制台</span>
      </div>
      <el-menu :default-active="activeMenu" background-color="#436193" text-color="#fff" active-text-color="#a8f49d" @select="handleSelect">
        <el-menu-item index="dashboard"><i class="el-icon-menu"></i>业务操作台</el-menu-item>
        <el-menu-item index="certs"><i class="el-icon-document"></i>全局绿证库</el-menu-item>
        <el-menu-item index="logs"><i class="el-icon-time"></i>流转审计日志</el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 右侧主区域 -->
    <el-container>
      <!-- 顶部 Header -->
      <el-header class="header">
        <div class="header-left">
          <strong>{{ currentUser.companyName }}</strong> (ID: {{ currentUser.id }})
          <span class="address-tag">{{ currentUser.chainAddress }}</span>
        </div>
        <el-button type="danger" size="small" @click="logout">退出系统</el-button>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">

        <!-- 视图 1: 业务操作台 (原有功能) -->
        <div v-show="activeMenu === 'dashboard'">
          <el-row :gutter="20">
            <!-- 核发模块 -->
            <el-col :span="12">
              <el-card shadow="hover" class="box-card admin-card">
                <template #header><b>1. 核发绿证 (上链存证)</b></template>
                <el-input v-model="issueForm.certId" placeholder="证书编号 (如: GC-001)" class="mb-10"></el-input>
                <el-input v-model="issueForm.ownerId" type="number" placeholder="接收企业ID (如: 2)" class="mb-10"></el-input>
                <el-input v-model="issueForm.energyType" placeholder="能源类型 (如: 光伏)" class="mb-10"></el-input>
                <el-input v-model="issueForm.amount" type="number" placeholder="电量 (MWh)" class="mb-10"></el-input>
                <el-button type="primary" class="btn-green w-100" @click="issueCert">一键上链核发</el-button>
              </el-card>
            </el-col>

            <!-- 防篡改核验 -->
            <el-col :span="12">
              <el-card shadow="hover" class="box-card">
                <template #header><b>2. 绿证链上防篡改核验</b></template>
                <el-input v-model="verifyCertId" placeholder="输入要查验的证书编号" class="mb-10"></el-input>
                <el-button type="primary" class="w-100" @click="verifyCert">交叉比对 (MySQL vs 区块链)</el-button>
                <div v-if="verifyResult" :class="['result-box', verifyResult.success ? 'success-box' : 'error-box']">
                  {{ verifyResult.msg }}
                </div>
              </el-card>

              <!-- 作废模块 -->
              <el-card shadow="hover" class="box-card admin-card mt-20">
                <template #header><b style="color: #f56c6c;">3. 异常纠错：作废绿证</b></template>
                <el-input v-model="revokeCertId" placeholder="输入要作废的证书编号" class="mb-10"></el-input>
                <el-button type="danger" class="w-100" @click="revokeCert">强制逻辑作废</el-button>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 视图 2: 全局绿证库 (新增) -->
        <div v-show="activeMenu === 'certs'">
          <el-card shadow="hover">
            <template #header><b>全局绿证资产库 (MySQL 同步)</b></template>
            <el-table :data="certList" border style="width: 100%">
              <el-table-column prop="certId" label="证书编号" width="150"></el-table-column>
              <el-table-column prop="energyType" label="能源类型" width="120"></el-table-column>
              <el-table-column prop="amount" label="电量(MWh)" width="120"></el-table-column>
              <el-table-column prop="currentOwnerId" label="当前持有人ID" width="120"></el-table-column>
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

        <!-- 视图 3: 流转审计日志 (新增) -->
        <div v-show="activeMenu === 'logs'">
          <el-card shadow="hover">
            <template #header><b>流转审计日志 (MySQL 同步)</b></template>
            <el-table :data="logList" border style="width: 100%">
              <el-table-column prop="id" label="流水号" width="80"></el-table-column>
              <el-table-column prop="certId" label="流转证书编号" width="150"></el-table-column>
              <el-table-column prop="fromOwnerId" label="卖方ID" width="100"></el-table-column>
              <el-table-column prop="toOwnerId" label="买方ID" width="100"></el-table-column>
              <el-table-column prop="transferTime" label="流转时间" :formatter="formatDate"></el-table-column>
              <el-table-column prop="txHash" label="上链哈希" show-overflow-tooltip></el-table-column>
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
  name: 'AdminDashboard',
  data() {
    return {
      activeMenu: 'dashboard',
      currentUser: JSON.parse(localStorage.getItem('currentUser') || '{}'),
      issueForm: { certId: '', ownerId: '', energyType: '', amount: '' },
      revokeCertId: '',
      verifyCertId: '',
      verifyResult: null,
      certList: [], // 绿证列表数据
      logList: []   // 日志列表数据
    }
  },
  created() {
    this.fetchCerts();
    this.fetchLogs();
  },
  methods: {
    handleSelect(index) {
      this.activeMenu = index;
      if (index === 'certs') this.fetchCerts();
      if (index === 'logs') this.fetchLogs();
    },
    logout() {
      localStorage.removeItem('currentUser');
      this.$router.push('/login');
    },
    formatDate(row, column, cellValue) {
      if (!cellValue) return '';
      return new Date(cellValue).toLocaleString();
    },
    // --- 新增：获取数据库列表 ---
    async fetchCerts() {
      try { this.certList = await request.get('/api/cert/listAll'); } catch (e) { console.error(e); }
    },
    async fetchLogs() {
      try { this.logList = await request.get('/api/cert/logs'); } catch (e) { console.error(e); }
    },
    // --- 原有业务逻辑保持不变 ---
    async issueCert() {
      if(!this.issueForm.certId || !this.issueForm.ownerId) return alert("请填写完整");
      try {
        const msg = await request.post('/api/cert/issue', this.issueForm);
        alert("✅ " + msg);
        this.issueForm = { certId: '', ownerId: '', energyType: '', amount: '' };
        this.fetchCerts(); // 刷新列表
      } catch (err) { alert("❌ " + err.message); }
    },
    async revokeCert() {
      if(!this.revokeCertId) return alert("请输入编号");
      if(!confirm("确定作废吗？此操作将记录在区块链上且不可逆！")) return;
      try {
        const msg = await request.post('/api/cert/revoke?certId=' + this.revokeCertId);
        alert("🛑 " + msg);
        this.fetchCerts(); // 刷新列表
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
.layout-container { height: 100vh; }
.sidebar { background-color: #436193; color: white; }
.logo { height: 60px; line-height: 60px; text-align: center; font-weight: bold; border-bottom: 1px solid rgba(255,255,255,0.1); }
.header { background-color: #fff; border-bottom: 1px solid #e6e6e6; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; }
.address-tag { background: #f0f9eb; color: #2ac079; padding: 2px 8px; border-radius: 4px; font-size: 12px; margin-left: 10px; border: 1px solid #e1f3d8;}
.main-content { background-color: #f0f2f5; padding: 20px; }
.mb-10 { margin-bottom: 10px; }
.mt-20 { margin-top: 20px; }
.w-100 { width: 100%; }
.btn-green { background-color: #2ac079; border-color: #2ac079; }
.btn-green:hover { background-color: #23a065; border-color: #23a065; }
.admin-card { border-top: 4px solid #f56c6c; }
.result-box { margin-top: 15px; padding: 15px; border-radius: 4px; font-weight: bold; }
.success-box { background: #e8f5e9; border: 1px solid #c8e6c9; color: #2e7d32; }
.error-box { background: #ffebee; border: 1px solid #ffcdd2; color: #c62828; }
</style>