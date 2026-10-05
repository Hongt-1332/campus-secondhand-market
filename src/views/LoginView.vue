<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">{{ isLogin ? '用户登录' : '用户注册' }}</h2>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" clearable />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <!-- 登录才显示管理员密码输入框，注册隐藏 -->
        <el-form-item v-if="isLogin" label="管理员密码">
          <el-input 
            v-model="form.adminPwd" 
            type="password" 
            show-password 
            placeholder="普通用户留空，管理员填写密码" 
            clearable 
          />
        </el-form-item>

        <el-form-item v-if="!isLogin" label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>

        <el-form-item v-if="!isLogin" label="联系方式" prop="contact">
          <el-input v-model="form.contact" placeholder="请输入手机号/微信" clearable />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleSubmit">
            {{ isLogin ? '登录' : '注册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="switch-text">
        <span @click="toggleMode">
          {{ isLogin ? '还没有账号？立即注册' : '已有账号？去登录' }}
        </span>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const isLogin = ref(true)

const form = reactive({
  username: '',
  password: '',
  adminPwd: '', // 管理员密码字段
  confirmPassword: '',
  contact: ''
})

// 表单校验规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6 到 20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  contact: [
    { required: true, message: '请输入联系方式', trigger: 'blur' }
  ]
}

// 切换登录/注册，清空管理员密码
const toggleMode = () => {
  isLogin.value = !isLogin.value
  form.adminPwd = ''
  formRef.value?.resetFields()
}

// 提交登录/注册
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      if (isLogin.value) {
        // 登录接口携带管理员密码
        const res = await request.post('/user/login', {
          username: form.username,
          password: form.password,
          adminPwd: form.adminPwd
        })
if (res.code === 200) {
  ElMessage.success('登录成功')
  // 关键：存入本地存储，模板才能识别登录状态
  localStorage.setItem('userInfo', JSON.stringify(res.data))
  request.defaults.headers.common['loginUser'] = JSON.stringify(res.data)
  router.push('/product')
        } else {
          ElMessage.error(res.msg || '登录失败')
        }
      } else {
        // 注册逻辑不变
        const res = await request.post('/user/register', {
          username: form.username,
          password: form.password,
          contact: form.contact
        })
        if (res.code === 200) {
          ElMessage.success('注册成功，请登录')
          isLogin.value = true
          form.adminPwd = ''
          formRef.value?.resetFields()
        } else {
          ElMessage.error(res.msg || '注册失败')
        }
      }
    } catch (err) {
      ElMessage.error('网络请求失败，请检查后端是否启动')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: calc(100vh - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
}
.login-card {
  width: 420px;
  padding: 20px;
}
.title {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}
.switch-text {
  text-align: center;
  margin-top: 10px;
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
}
.switch-text:hover {
  text-decoration: underline;
}
</style>