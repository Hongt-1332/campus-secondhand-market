<template>
  <div class="page-wrap" style="padding:20px;max-width:1400px;margin:0 auto;">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:15px;">
      <h2>用户管理列表</h2>
      <el-button type="primary" @click="openAdd">新增用户</el-button>
    </div>

    <!-- 搜索区域 -->
    <el-card style="margin-bottom:15px;">
      <el-form inline :model="search">
        <el-form-item label="用户名">
          <el-input v-model="search.username" clearable placeholder="输入用户名搜索"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="search={};handleSearch()">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户表格 -->
    <el-table :data="pagedData" border stripe v-loading="loading">
      <el-table-column label="用户ID" prop="id" width="80"></el-table-column>
      <el-table-column label="用户名" prop="username"></el-table-column>
      <el-table-column label="账号角色" prop="isAdmin">
        <template #default="scope">
          <el-tag v-if="scope.row.isAdmin === 1" type="danger">管理员</el-tag>
          <el-tag v-else type="info">普通用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" type="primary" @click="openEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页控件 -->
    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top:15px;text-align:right"
      @change="handlePageChange"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="用户名" prop="username" rules="[{required:true,message:'用户名不能为空'}]">
          <el-input v-model="form.username"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="isAdd" rules="[{required:true,message:'新增用户必须填写密码'}]">
          <el-input v-model="form.password" show-password></el-input>
        </el-form-item>
        <el-form-item label="账号角色">
          <el-radio-group v-model="form.isAdmin">
            <el-radio :label="0">普通用户</el-radio>
            <el-radio :label="1">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const search = ref({ username: '' })

// 后端分页：直接使用接口返回的当前页数据
const pagedData = computed(() => tableData.value)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isAdd = ref(true)
const formRef = ref(null)
const form = ref({
  id: null,
  username: '',
  password: '',
  isAdmin: 0
})

// 获取列表（支持后端分页）
const getList = async () => {
  loading.value = true
  const res = await request.get('/user/list', {
    params: {
      username: search.value.username,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  })
  loading.value = false
  if (res.code === 200) {
    const isArray = Array.isArray(res.data)
    const data = isArray ? res.data : (res.data?.list || [])
    tableData.value = data
    total.value = isArray ? data.length : (res.data?.total || data.length)
  } else {
    ElMessage.error(res.msg || '用户列表加载失败')
    tableData.value = []
    total.value = 0
  }
}

// 搜索时重置到第一页
const handleSearch = () => {
  pageNum.value = 1
  getList()
}

// 切换页码/每页条数时重新请求
const handlePageChange = () => {
  getList()
}

// 打开新增弹窗
const openAdd = () => {
  dialogTitle.value = '新增用户'
  isAdd.value = true
  form.value = { id: null, username: '', password: '', isAdmin: 0 }
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEdit = (row) => {
  dialogTitle.value = '修改用户信息'
  isAdd.value = false
  form.value = { ...row, password: '' }
  dialogVisible.value = true
}

// 提交新增/编辑
const submitForm = async () => {
  await formRef.value.validate()
  let res
  if (isAdd.value) {
    res = await request.post('/user', form.value)
  } else {
    res = await request.put('/user', form.value)  
  }
  if (res.code === 200) {
    ElMessage.success(isAdd.value ? '新增用户成功' : '修改用户成功')
    dialogVisible.value = false
    getList()
  } else {
    ElMessage.error(res.msg || '操作失败')
  }
}

// 删除用户
const handleDelete = (userId) => {
  ElMessageBox.confirm('确定要删除该用户？删除后无法恢复', '警告', {
    type: 'warning'
  }).then(async () => {
    const res = await request.delete(`/user/${userId}`)
    if (res.code === 200) {
      ElMessage.success('删除用户成功')
      getList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

onMounted(() => getList())
</script>