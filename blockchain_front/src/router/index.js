import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../views/AdminDashboard.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' } // 标记该路由仅限 ADMIN
  },
  {
    path: '/corp',
    name: 'CorpDashboard',
    component: () => import('../views/CorpDashboard.vue'),
    meta: { requiresAuth: true, role: 'CORP' } // 标记该路由仅限 CORP
  }
]

// 【核心修改】Vue 3 使用 createRouter 创建路由实例
const router = createRouter({
  history: createWebHistory(), // 使用 HTML5 模式
  routes
})

// 全局路由守卫：拦截未登录和越权访问
router.beforeEach((to, from, next) => {
  const userStr = localStorage.getItem('currentUser');

  if (to.meta.requiresAuth) {
    if (!userStr) {
      // 没登录，踢回登录页
      return next('/login');
    }

    const user = JSON.parse(userStr);
    // 检查角色权限
    if (to.meta.role && to.meta.role !== user.role) {
      alert("越权访问！您没有该页面的权限。");
      // 根据他的真实角色把他送回他该去的地方
      return next(user.role === 'ADMIN' ? '/admin' : '/corp');
    }
  }
  next();
});

export default router;