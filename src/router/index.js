import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/product',
    component: () => import('../views/ProductView.vue')
  },
  {
    path: '/admin/user',
    name: 'UserManage',
    component: () => import('../views/UserManage.vue')
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/ProfileView.vue')
  },
  // 关键：新增购物车路由
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/CartView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStr = localStorage.getItem('userInfo')
  // 未登录拦截
  if (!userStr) {
    if (to.path === '/login') {
      next()
    } else {
      next('/login')
    }
    return
  }

  const user = JSON.parse(userStr)
  // 普通用户禁止访问管理员页面
  if (to.path.startsWith('/admin') && user.isAdmin !== 1) {
    next('/product')
    return
  }

  next()
})

export default router