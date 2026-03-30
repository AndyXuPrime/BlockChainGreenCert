<template>
  <div class="login-wrapper">
    <!-- 动态粒子背景层 -->
    <canvas ref="particleCanvas" id="particle-canvas"></canvas>

    <!-- 登录卡片层 -->
    <div class="login-container">
      <div class="login-card">

        <!-- 顶部 Logo 区 -->
        <div class="logo-area">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="12" r="2" fill="#2ac079" stroke="white" stroke-width="1"/>
            </svg>
          </div>
          <h1>绿证链 · 绿电溯源</h1>
          <div class="subtitle">Green Certificate · 可信流转</div>
        </div>

        <!-- 模式切换 Tabs -->
        <div class="nav-tabs">
          <span @click="authMode = 'login'" :class="{active: authMode === 'login'}">系统登录</span>
          <span @click="authMode = 'register'" :class="{active: authMode === 'register'}">企业注册</span>
        </div>

        <!-- ================= 登录表单 ================= -->
        <div v-if="authMode === 'login'" class="form-area">
          <!-- 账号输入框组 -->
          <div class="input-group">
            <div class="input-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
            </div>
            <input type="text" v-model="loginForm.username" placeholder="账号 (如: admin)" @keyup.enter="onLoginClick">
          </div>

          <!-- 密码输入框组 -->
          <div class="input-group">
            <div class="input-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
            </div>
            <input type="password" v-model="loginForm.password" placeholder="密码" @keyup.enter="onLoginClick">
          </div>

          <button class="login-btn" @click="onLoginClick">登 录 系 统</button>
        </div>

        <!-- ================= 注册表单 ================= -->
        <div v-if="authMode === 'register'" class="form-area">
          <div class="input-group">
            <div class="input-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" />
              </svg>
            </div>
            <input type="text" v-model="regForm.username" placeholder="设置登录账号 (如: corp_a)">
          </div>

          <div class="input-group">
            <div class="input-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" /><path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
            </div>
            <input type="password" v-model="regForm.password" placeholder="设置登录密码">
          </div>

          <div class="input-group">
            <div class="input-icon">
              <!-- 企业建筑图标 -->
              <svg viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="1.8">
                <path d="M3 21h18"></path><path d="M5 21V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16"></path><path d="M9 7h6"></path><path d="M9 11h6"></path><path d="M9 15h6"></path>
              </svg>
            </div>
            <input type="text" v-model="regForm.companyName" placeholder="企业真实名称 (如: 深圳新能源)">
          </div>

          <button class="login-btn" @click="onRegisterClick">注 册 并 上 链</button>
        </div>

        <!-- 错误提示区 -->
        <div class="error-msg">{{ errorMessage }}</div>

        <!-- 底部 -->
        <div class="footer-links">
          <span>🌱 基于 FISCO BCOS</span>
          <span>链上查验 · 永久存证</span>
        </div>

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
      regForm: { username: '', password: '', companyName: '' },
      errorMessage: '',

      canvasCtx: null,
      particles: [],
      mouseX: null,
      mouseY: null,
      animationId: null
    }
  },
  mounted() {
    this.initParticleSystem();
  },
  beforeUnmount() {
    cancelAnimationFrame(this.animationId);
    window.removeEventListener('resize', this.resizeCanvas);
    window.removeEventListener('mousemove', this.trackMouse);
  },
  methods: {
    showError(msg) {
      this.errorMessage = msg;
      setTimeout(() => { if (this.errorMessage === msg) this.errorMessage = ''; }, 3000);
    },

    onLoginClick(e) {
      if(e && e.clientX) this.createRipple(e);
      this.handleLogin();
    },
    onRegisterClick(e) {
      if(e && e.clientX) this.createRipple(e);
      this.handleRegister();
    },

    async handleLogin() {
      if(!this.loginForm.username || !this.loginForm.password) {
        this.showError('❌ 请输入账号和密码');
        this.shakeCard();
        return;
      }
      try {
        const user = await request.post('/auth/login', this.loginForm);
        localStorage.setItem('currentUser', JSON.stringify(user));
        this.showError('✅ 登录成功，正在跳转...');

        setTimeout(() => {
          if (user.role === 'ADMIN') this.$router.push('/admin');
          else this.$router.push('/corp');
        }, 800);
      } catch (err) {
        this.showError("❌ " + err.message);
        this.shakeCard();
      }
    },

    async handleRegister() {
      if(!this.regForm.username || !this.regForm.password || !this.regForm.companyName) {
        this.showError('❌ 请填写完整信息');
        this.shakeCard();
        return;
      }
      try {
        await request.post('/auth/register', this.regForm);
        alert("🎉 注册成功！区块链账户已生成，请登录。");
        this.authMode = 'login';
        this.regForm = { username: '', password: '', companyName: '' };
      } catch (err) {
        this.showError("❌ 注册失败: " + err.message);
        this.shakeCard();
      }
    },

    shakeCard() {
      const card = document.querySelector('.login-card');
      if(card) {
        card.style.transform = 'translateX(8px)';
        setTimeout(() => { card.style.transform = 'translateX(-8px)'; }, 100);
        setTimeout(() => { card.style.transform = 'translateX(0)'; }, 200);
      }
    },

    createRipple(event) {
      const btn = event.currentTarget;
      const ripple = document.createElement('span');
      const rect = btn.getBoundingClientRect();
      const size = Math.max(rect.width, rect.height);
      const x = event.clientX - rect.left - size / 2;
      const y = event.clientY - rect.top - size / 2;
      ripple.style.width = ripple.style.height = `${size}px`;
      ripple.style.left = `${x}px`;
      ripple.style.top = `${y}px`;
      ripple.classList.add('ripple');
      btn.appendChild(ripple);
      setTimeout(() => ripple.remove(), 600);
    },

    // ==================== 粒子背景动画逻辑 ====================
    initParticleSystem() {
      const canvas = this.$refs.particleCanvas;
      this.canvasCtx = canvas.getContext('2d');
      this.resizeCanvas();

      this.particles = [];
      const colors = ['#2ac079', '#3ed98a', '#a8f49d', '#5fdc8c', '#1e9f62'];
      for (let i = 0; i < 110; i++) {
        this.particles.push({
          x: Math.random() * canvas.width,
          y: Math.random() * canvas.height,
          vx: (Math.random() - 0.5) * 0.4,
          vy: (Math.random() - 0.5) * 0.3,
          size: Math.random() * 2.2 + 1.2,
          color: colors[Math.floor(Math.random() * colors.length)],
          alpha: Math.random() * 0.5 + 0.3
        });
      }

      window.addEventListener('resize', this.resizeCanvas);
      window.addEventListener('mousemove', this.trackMouse);
      window.addEventListener('mouseleave', () => { this.mouseX = null; this.mouseY = null; });

      this.animateParticles();
    },

    resizeCanvas() {
      const canvas = this.$refs.particleCanvas;
      if(canvas) {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
      }
    },

    trackMouse(e) {
      this.mouseX = e.clientX;
      this.mouseY = e.clientY;
    },

    animateParticles() {
      const canvas = this.$refs.particleCanvas;
      const ctx = this.canvasCtx;
      if (!ctx || !canvas) return;

      ctx.clearRect(0, 0, canvas.width, canvas.height);
      ctx.fillStyle = 'rgba(15, 42, 28, 0.12)';
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      const MOUSE_RADIUS = 120;
      const CONNECT_DISTANCE = 130;

      for (let i = 0; i < this.particles.length; i++) {
        let p = this.particles[i];

        if (p.x < 0 || p.x > canvas.width) p.vx *= -0.9;
        if (p.y < 0 || p.y > canvas.height) p.vy *= -0.9;

        if (this.mouseX !== null && this.mouseY !== null) {
          const dx = p.x - this.mouseX;
          const dy = p.y - this.mouseY;
          const dist = Math.hypot(dx, dy);
          if (dist < MOUSE_RADIUS) {
            const force = (MOUSE_RADIUS - dist) / MOUSE_RADIUS * 1.2;
            p.vx += (dx / dist) * force * 0.5;
            p.vy += (dy / dist) * force * 0.5;
          }
        }

        p.vx *= 0.99; p.vy *= 0.99;
        if (Math.abs(p.vx) > 1.8) p.vx = p.vx > 0 ? 1.8 : -1.8;
        if (Math.abs(p.vy) > 1.8) p.vy = p.vy > 0 ? 1.8 : -1.8;

        p.x += p.vx;
        p.y += p.vy;

        ctx.beginPath();
        ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
        ctx.fillStyle = p.color;
        ctx.globalAlpha = p.alpha;
        ctx.fill();
      }

      ctx.globalAlpha = 1;
      for (let i = 0; i < this.particles.length; i++) {
        for (let j = i + 1; j < this.particles.length; j++) {
          const dx = this.particles[i].x - this.particles[j].x;
          const dy = this.particles[i].y - this.particles[j].y;
          const dist = Math.hypot(dx, dy);
          if (dist < CONNECT_DISTANCE) {
            ctx.beginPath();
            ctx.moveTo(this.particles[i].x, this.particles[i].y);
            ctx.lineTo(this.particles[j].x, this.particles[j].y);
            ctx.strokeStyle = `rgba(42, 192, 121, ${(1 - dist / CONNECT_DISTANCE) * 0.25})`;
            ctx.lineWidth = 1;
            ctx.stroke();
          }
        }
      }

      this.animationId = requestAnimationFrame(this.animateParticles);
    }
  }
}
</script>

<style scoped>
/* ================= 全局 & 背景 ================= */
* { box-sizing: border-box; }

.login-wrapper {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  margin: -20px; /* 抵消 App.vue 里的 body padding */
}

#particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  background: linear-gradient(135deg, #182a0f 0%, #1a3f2c 50%, #0b2a22 100%);
}

/* ================= 卡片容器 (毛玻璃) ================= */
.login-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 10;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-card {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 32px;
  padding: 48px 40px;
  width: 420px;
  max-width: 90%;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(42, 192, 121, 0.2);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  animation: fadeInUp 0.8s cubic-bezier(0.2, 0.9, 0.4, 1.1);
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 30px 60px -12px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(42, 192, 121, 0.5);
}

/* ================= Logo & 标题 ================= */
.logo-area { text-align: center; margin-bottom: 30px; }
.logo-icon {
  width: 70px; height: 70px;
  background: linear-gradient(145deg, #2ac079, #1e9f62);
  border-radius: 28px;
  display: inline-flex; align-items: center; justify-content: center;
  margin-bottom: 15px;
  box-shadow: 0 10px 20px -5px rgba(42, 192, 121, 0.4);
  animation: float 3s ease-in-out infinite;
}
.logo-icon svg { width: 42px; height: 42px; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2)); }
h1 { color: white; font-size: 26px; font-weight: 600; margin-bottom: 8px; letter-spacing: 1px;}
.subtitle { color: rgba(255, 255, 255, 0.7); font-size: 14px; letter-spacing: 1px; }

/* ================= 导航 Tabs ================= */
.nav-tabs {
  display: flex; justify-content: center; margin-bottom: 25px;
}
.nav-tabs span {
  cursor: pointer; margin: 0 15px; color: rgba(255,255,255,0.5); font-size: 16px; font-weight: bold; transition: 0.3s; padding-bottom: 8px;
}
.nav-tabs span.active { color: #2ac079; border-bottom: 2px solid #2ac079; }
.nav-tabs span:hover { color: white; }

/* ================= 输入框组 (核心修改点) ================= */
.input-group {
  display: flex;
  align-items: center;
  width: 100%;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 48px;
  padding: 0 18px;
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

/* 当输入框获得焦点时，高亮整个父容器 */
.input-group:focus-within {
  border-color: #2ac079;
  background: rgba(255, 255, 255, 0.15);
  box-shadow: 0 0 0 3px rgba(42, 192, 121, 0.2);
}

.input-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  width: 20px;
  height: 20px;
  opacity: 0.6;
}

.input-group input {
  flex: 1; /* 占据剩余全部空间 */
  background: transparent; /* 背景透明 */
  border: none; /* 无边框 */
  padding: 15px 0; /* 只有上下内边距 */
  font-size: 15px;
  color: white;
  outline: none;
  font-weight: 500;
  width: 100%;
}

.input-group input::placeholder { color: rgba(255, 255, 255, 0.5); font-weight: 400; }

/* ================= 按钮 ================= */
.login-btn {
  width: 100%; padding: 15px;
  background: linear-gradient(90deg, #2ac079, #1e9f62);
  border: none; border-radius: 48px; color: white; font-size: 16px; font-weight: 700;
  cursor: pointer; margin-top: 10px; transition: all 0.3s ease;
  position: relative; overflow: hidden; letter-spacing: 2px;
  box-shadow: 0 4px 15px rgba(42, 192, 121, 0.3);
}
.login-btn:hover { transform: scale(1.02); box-shadow: 0 8px 25px rgba(42, 192, 121, 0.5); }
.login-btn:active { transform: scale(0.98); }

/* 波纹特效 */
:deep(.ripple) {
  position: absolute; border-radius: 50%; background: rgba(255, 255, 255, 0.5);
  transform: scale(0); animation: rippleEffect 0.6s linear; pointer-events: none;
}

/* ================= 底部 & 错误信息 ================= */
.error-msg { color: #ff9e8f; font-size: 14px; margin-top: 15px; text-align: center; min-height: 20px; font-weight: bold;}
.footer-links { display: flex; justify-content: space-between; margin-top: 25px; font-size: 13px; color: rgba(255, 255, 255, 0.5); }

/* ================= 动画关键帧 ================= */
@keyframes fadeInUp { from { opacity: 0; transform: translateY(40px); } to { opacity: 1; transform: translateY(0); } }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-6px); } }
@keyframes rippleEffect { to { transform: scale(4); opacity: 0; } }
</style>