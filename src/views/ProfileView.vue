<template>
  <div class="profile-wrap" style="padding:40px;max-width:600px;margin:80px auto 0;">
    <el-card>
      <h2 style="text-align:center;margin-bottom:30px;">个人资料</h2>
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="个人昵称" prop="nickname" :rules="[{required:true,message:'请填写昵称',trigger:'blur'}]">
          <el-input v-model="form.nickname" placeholder="请输入你的昵称" clearable></el-input>
        </el-form-item>
        <el-form-item label="联系方式" prop="contact" :rules="[{required:true,message:'请填写手机号/微信',trigger:'blur'}]">
          <el-input v-model="form.contact" placeholder="手机号或微信号" clearable></el-input>
        </el-form-item>
        <el-form-item label="宿舍地址" prop="address" :rules="[{required:true,message:'请填写宿舍地址',trigger:'blur'}]">
          <el-input v-model="form.address" placeholder="例如：3号楼 201室" clearable></el-input>
        </el-form-item>
        <el-form-item style="text-align:center;margin-top:30px;">
          <el-button type="primary" @click="submitInfo" :loading="loading">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = ref({
  id: null,
  username: '',
  nickname: '',
  contact: '',
  address: ''
})

// 页面加载，回显当前用户资料
const loadUserInfo = async () => {
  const res = await request.get('/user/profile')
  if (res.code === 200) {
    form.value = res.data
  }
}

// 提交保存资料
const submitInfo = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await request.put('/user/profile', form.value)
    if (res.code === 200) {
      ElMessage.success('个人资料保存成功')
      // 同步更新 localStorage 中的昵称
      const userStr = localStorage.getItem('userInfo')
      if (userStr) {
        const userInfo = JSON.parse(userStr)
        userInfo.nickname = form.value.nickname
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
      }
    } else {
      ElMessage.error(res.msg || '保存失败')
    }
  } catch (err) {
    ElMessage.error('网络异常')
  } finally {
    loading.value = false
  }
}

onMounted(() => loadUserInfo())
</script>