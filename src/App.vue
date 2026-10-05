<template>
  <div id="app">
    <!-- 顶部导航栏 -->
    <div class="nav-bar">
      <span class="logo">校园二手交易平台</span>
      <div class="nav-right">
        <span class="nav-item" @click="$router.push('/product')">商品列表</span>
        <span class="nav-item" @click="$router.push('/cart')">购物车</span>
        <span class="nav-item" @click="$router.push('/profile')">个人资料</span>

        <!-- 管理员专属菜单 -->
        <template v-if="hasLogin && loginUser.isAdmin === 1">
          <span class="nav-item" @click="$router.push('/admin/user')">用户管理</span>
        </template>

        <!-- 未登录展示登录入口 -->
        <span v-if="!hasLogin" class="nav-item" @click="$router.push('/login')">登录/注册</span>

        <!-- 已登录用户信息 -->
        <div v-else class="user-group">
          <span class="nav-item">
            欢迎{{ loginUser.username }}
            <span v-if="loginUser.isAdmin === 1">(管理员)</span>
          </span>
          <span class="nav-item logout-btn" @click="logout">退出登录</span>
        </div>
      </div>
    </div>

    <!-- 页面路由内容 -->
    <router-view />

    <!-- 可拖动悬浮智能客服按钮 -->
    <div
      ref="floatBtnRef"
      class="service-float-btn"
      :style="{ left: floatX + 'px', top: floatY + 'px' }"
      @mousedown="startDrag"
    >
      🤖 智能客服
    </div>

    <!-- 客服对话弹窗 -->
    <el-dialog v-model="dialogServiceVisible" title="智能客服助手" width="500px">
      <div class="chat-box">
        <div v-for="(item, index) in chatList" :key="index" class="chat-item" :class="item.type">
          <div class="chat-text">{{ item.msg }}</div>
        </div>
      </div>
      <div class="chat-input">
        <el-input v-model="userInput" placeholder="输入你的问题" @keyup.enter="sendMsg"></el-input>
        <el-button type="primary" @click="sendMsg" style="margin-left:8px;">发送</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, watch, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from './utils/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const refreshKey = ref(0)

// 获取本地登录用户
const getUserStorage = () => {
  refreshKey.value
  const str = localStorage.getItem('userInfo')
  if (!str) return {}
  try {
    return JSON.parse(str)
  } catch (e) {
    return {}
  }
}

const loginUser = computed(() => getUserStorage())
const hasLogin = computed(() => !!localStorage.getItem('userInfo'))

// 退出登录
const logout = () => {
  localStorage.removeItem('userInfo')
  delete request.defaults.headers.common['loginUser']
  refreshKey.value++
  ElMessage.success('已退出登录')
  router.push('/login')
}

watch(() => route.fullPath, () => {
  refreshKey.value++
})

// ====================== 智能客服基础配置 ======================
const dialogServiceVisible = ref(false)
const userInput = ref('')
const chatList = ref([
  { type: 'robot', msg: '你好！我是校园二手平台智能客服，有任何问题都可以问我~' }
])
// 发送消息调用AI接口（已修改代理地址）
const sendMsg = async () => {
  const msg = userInput.value.trim()
  if (!msg) return
  chatList.value.push({ type: 'user', msg })
  userInput.value = ''
  try {
    const res = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        messages: [
          { role: 'system', content: '你是校园二手交易平台专属智能客服，只回答平台相关问题：发布商品、购物车、个人资料、登录注册、商品购买等，简洁回复。' },
          { role: 'user', content: msg }
        ]
      })
    })
    const rawText = await res.text()
    console.log('接口原始返回内容：', rawText)
    if (!res.ok) {
      chatList.value.push({ type: 'robot', msg: `接口请求失败，状态码：${res.status}` })
      return
    }
    const data = JSON.parse(rawText)
    // 后端直接返回纯文本回答
    const reply = data.data
    chatList.value.push({ type: 'robot', msg: reply })
  } catch (err) {
    console.error('AI接口完整报错：', err)
    chatList.value.push({ type: 'robot', msg: '网络请求失败，请检查网络或API密钥' })
  }
}
// ====================== 拖动悬浮按钮逻辑 ======================
const floatBtnRef = ref(null)
const floatX = ref(20)
const floatY = ref(100)
let isDrag = false
let offsetX = 0
let offsetY = 0

// 鼠标按下，开始拖动
const startDrag = (e) => {
  isDrag = false
  offsetX = e.clientX - floatX.value
  offsetY = e.clientY - floatY.value
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', stopDrag)
}

// 拖动过程实时更新坐标，限制边界
const onMouseMove = (e) => {
  isDrag = true
  const winW = window.innerWidth
  const winH = window.innerHeight
  const btnW = floatBtnRef.value?.offsetWidth || 140
  const btnH = floatBtnRef.value?.offsetHeight || 44

  let newX = e.clientX - offsetX
  let newY = e.clientY - offsetY
  // 左右边界限制
  newX = Math.max(0, Math.min(newX, winW - btnW))
  // 顶部避开导航栏(60px)，底部不超出窗口
  newY = Math.max(70, Math.min(newY, winH - btnH))

  floatX.value = newX
  floatY.value = newY
}

// 停止拖动，移除全局监听
const stopDrag = () => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', stopDrag)
  // 仅轻点（未拖动）才打开弹窗
  if (!isDrag) {
    dialogServiceVisible.value = true
  }
}

// 页面销毁清除监听，防止内存泄漏
onUnmounted(() => {
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', stopDrag)
})
</script>

<style scoped>
/* 顶部导航栏 */
.nav-bar {
  width: 100%;
  height: 60px;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 999;
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 30px;
  padding-right: 140px;
}
.nav-item {
  line-height: 60px;
  cursor: pointer;
  font-size: 15px;
}
.user-group {
  display: flex;
  align-items: center;
  gap: 30px;
}
.logout-btn {
  color: #ffcccc;
}
#app {
  padding-top: 60px;
}

/* 可拖动悬浮客服按钮 */
.service-float-btn {
  position: fixed;
  background: #409eff;
  color: white;
  padding: 10px 16px;
  border-radius: 20px;
  cursor: grab;
  z-index: 998;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.13);
  user-select: none;
  transition: background 0.2s;
}
.service-float-btn:active {
  cursor: grabbing;
}
.service-float-btn:hover {
  background: #66b1ff;
}

/* 聊天弹窗样式 */
.chat-box {
  height: 350px;
  overflow-y: auto;
  border: 1px solid #eee;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 4px;
}
.chat-item {
  margin: 8px 0;
  max-width: 80%;
}
.chat-item.user {
  text-align: right;
}
.chat-item.robot {
  text-align: left;
}
.chat-text {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 12px;
}
.chat-item.user .chat-text {
  background: #409eff;
  color: #fff;
}
.chat-item.robot .chat-text {
  background: #f5f7fa;
  color: #333;
}
.chat-input {
  display: flex;
  align-items: center;
}
</style>