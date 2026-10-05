<template>
  <div class="product-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchName"
        placeholder="搜索商品名称"
        clearable
        style="width: 300px"
        @keyup.enter="getList"
      />
      <el-button type="primary" @click="getList">搜索</el-button>
      <el-button type="success" @click="handleAdd">发布商品</el-button>
    </div>

    <el-table :data="tableData" border stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="商品名称" width="200" />
      <el-table-column prop="description" label="商品描述" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="scope">¥{{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="seller" label="卖家" width="120" />
      <el-table-column prop="contact" label="联系方式" width="150" />
      <el-table-column prop="deliveryType" label="配送方式" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.deliveryType === 0 || scope.row.deliveryType === '0'" type="success">自取</el-tag>
          <el-tag v-else-if="scope.row.deliveryType === 1 || scope.row.deliveryType === '1'" type="primary">送到宿舍楼下</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <!-- 商品图片列 -->
      <el-table-column label="商品图片" width="120">
        <template #default="scope">
          <el-image
            v-if="scope.row.image"
            :src="scope.row.image"
            style="width:80px;height:80px;object-fit:cover"
            fit="cover"
            :preview-src-list="[scope.row.image]"
          />
          <span v-else>无图</span>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="发布时间" width="180" />

      <!-- 操作列：管理员全部显示，普通用户仅自己商品显示编辑删除 -->
      <el-table-column label="操作" width="260">
        <template #default="scope">
          <el-button type="success" size="small" @click="addCart(scope.row.id)">加入购物车</el-button>
          
          <!-- 两种情况显示编辑删除：1.管理员 2.商品是自己发布 -->
          <template v-if="isAdmin || scope.row.userId === currentUser.id">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑商品' : '发布商品'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="form.price" type="number" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option label="数码" value="数码" />
            <el-option label="图书" value="图书" />
            <el-option label="生活用品" value="生活用品" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <!-- 管理员编辑可改卖家，普通用户/发布商品禁用 -->
        <el-form-item label="卖家">
          <el-input 
            v-model="form.seller" 
            :disabled="!isAdmin" 
            placeholder="昵称取自个人资料页面" 
          />
          <div v-if="!isAdmin" style="font-size:12px;color:#999;margin-top:4px;">
            仅管理员可修改卖家，普通用户不可更改
          </div>
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input 
            v-model="form.contact" 
            :disabled="!isAdmin" 
            placeholder="取自个人资料页面，前往个人资料修改" 
          />
          <div v-if="!isAdmin" style="font-size:12px;color:#999;margin-top:4px;">
            可前往顶部「个人资料」页面修改联系方式
          </div>
        </el-form-item>
        <el-form-item label="配送方式">
          <el-radio-group v-model="form.deliveryType">
            <el-radio :value="0">自取</el-radio>
            <el-radio :value="1">送到宿舍楼下</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="商品图片">
          <!-- 上传组件 -->
          <el-upload
            action="/api/file/upload"
            :on-success="handleUploadSuccess"
            list-type="picture-card"
            :file-list="imgList"
            :limit="1"
          >
            <template #default>
              <div>
                <el-icon><Plus /></el-icon>
                <div class="el-upload__text">上传图片</div>
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../utils/request'

// 读取本地登录用户
let storageStr = localStorage.getItem('userInfo')
let userInfo = {}
if (storageStr) {
  userInfo = JSON.parse(storageStr)
}
// 当前登录完整用户
const currentUser = ref(userInfo)
// 是否管理员
const isAdmin = ref(userInfo.isAdmin === 1)

const searchName = ref('')
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
// 图片上传列表
const imgList = ref([])
const form = reactive({
  id: null,
  name: '',
  description: '',
  price: null,
  category: '',
  seller: '',
  contact: '',
  status: 0,
  image: '',
  deliveryType: 0
})

// 加载当前登录用户昵称、联系方式（发布商品自动填充）
const loadUserProfile = async () => {
  try {
    const res = await request.get('/user/profile')
    if (res.code === 200) {
      form.seller = res.data.nickname
      form.contact = res.data.contact
    }
  } catch (err) {
    ElMessage.error('获取个人资料失败，请重新登录')
  }
}

// 加入购物车
const addCart = async (productId) => {
  try {
    const res = await request.post(`/cart/add/${productId}`)
    if (res.code === 200) {
      ElMessage.success('成功加入购物车！可点击右上角购物车查看')
    }
  } catch (err) {
    ElMessage.error('加入失败，请先登录')
  }
}

// 图片上传回调
const handleUploadSuccess = (res) => {
  if (res.code === 200) {
    form.image = res.data
  }
}

// 获取商品列表
const getList = async () => {
  try {
    const res = await request.get('/product/list', {
      params: { name: searchName.value }
    })
    if (res.code === 200) {
      tableData.value = res.data
    } else {
      ElMessage.error(res.msg || '获取列表失败')
    }
  } catch (err) {
    ElMessage.error('网络请求失败，请检查后端是否启动')
  }
}

// 打开发布商品弹窗
const handleAdd = async () => {
  isEdit.value = false
  // 清空表单
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    price: null,
    category: '',
    seller: '',
    contact: '',
    status: 0,
    image: '',
    deliveryType: 0
  })
  imgList.value = []
  // 自动填充当前用户昵称、联系方式
  await loadUserProfile()
  dialogVisible.value = true
}

// 打开编辑弹窗
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  // 回显图片
  if (form.image) {
    imgList.value = [{ url: form.image }]
  } else {
    imgList.value = []
  }
  dialogVisible.value = true
}

// 提交新增/编辑
const submitForm = async () => {
  // 转换 price 为数字，避免后端接收字符串导致 400
  let priceVal = Number(form.price)
  if (isNaN(priceVal) || form.price === '' || form.price === null) {
    priceVal = 0
  }
  const submitData = {
    id: form.id,
    name: form.name,
    description: form.description,
    price: priceVal,
    category: form.category,
    seller: form.seller,
    contact: form.contact,
    status: form.status,
    image: form.image,
    deliveryType: form.deliveryType
  }
  console.log('提交数据：', JSON.parse(JSON.stringify(submitData)))
  try {
    let res
    if (isEdit.value) {
      // 编辑商品
      res = await request.put('/product', submitData)
    } else {
      // 发布商品
      res = await request.post('/product', submitData)
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '发布成功')
      dialogVisible.value = false
      getList()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (err) {
    console.error('提交失败详情：', err)
  console.error('后端响应数据：', JSON.stringify(err?.response?.data))
    console.error('HTTP状态码：', err?.response?.status)
    console.error('请求体：', JSON.stringify(submitData))
    const backendMsg = err?.response?.data?.msg || err?.response?.data?.message || err?.response?.data?.error
    const status = err?.response?.status
    if (status === 400 && backendMsg) {
      ElMessage.error('请求参数错误：' + backendMsg)
    } else if (status === 403) {
      ElMessage.error('权限不足，无法操作')
    } else if (status === 500) {
      ElMessage.error('服务器内部错误，请联系管理员')
    } else {
      ElMessage.error('操作失败，权限不足或网络异常')
    }
  }
}

// 删除商品
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete(`/product/${id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (err) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.product-page {
  padding: 20px;
}
.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>