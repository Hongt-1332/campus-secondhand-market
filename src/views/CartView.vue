<template>
  <div class="cart-wrap" style="padding:30px;max-width:1200px;margin:80px auto 0;">
    <h2 style="margin-bottom:20px;">我的购物车</h2>
    <!-- 判断数组长度展示表格/空提示 -->
    <el-table :data="cartList" border stripe v-if="cartList && cartList.length > 0">
      <el-table-column label="商品图片" width="120">
        <template #default="scope">
          <el-image
            v-if="scope.row.product && scope.row.product.image"
            :src="scope.row.product.image"
            style="width:80px;height:80px;object-fit:cover"
            fit="cover"
          />
          <span v-else>无图</span>
        </template>
      </el-table-column>
      <el-table-column label="商品名称">
        <template #default="scope">{{ scope.row.product?.name || "商品已下架" }}</template>
      </el-table-column>
      <el-table-column label="单价" width="100">
        <template #default="scope">¥{{ scope.row.product?.price || 0 }}</template>
      </el-table-column>
      <el-table-column label="数量" width="140">
        <template #default="scope">
          <el-input-number
            v-model="scope.row.num"
            :min="1"
            @change="changeNum(scope.row.id, scope.row.num)"
          />
        </template>
      </el-table-column>
      <el-table-column label="小计" width="120">
        <template #default="scope">
          ¥{{ ((scope.row.product?.price || 0) * scope.row.num).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="卖家" width="120">
        <template #default="scope">{{ scope.row.product?.seller || "未知卖家" }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button type="danger" size="small" @click="delCart(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空购物车提示 -->
    <div v-else style="text-align:center;padding:80px 0;color:#999;font-size:18px;">
      购物车暂无商品，快去挑选商品吧
      <el-button type="primary" style="margin-left:20px" @click="$router.push('/product')">去选购</el-button>
    </div>

    <!-- 底部结算栏 -->
    <div v-if="cartList && cartList.length > 0" style="margin-top:20px;display:flex;justify-content:space-between;align-items:center;padding:15px;border:1px solid #eee;">
      <div>合计：<span style="color:#f56c6c;font-size:20px;">¥{{ totalPrice }}</span></div>
      <div>
        <el-button type="danger" @click="clearAllCart">清空购物车</el-button>
        <el-button type="primary">立即结算</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const cartList = ref([])

// 计算总价
const totalPrice = computed(() => {
  let sum = 0
  cartList.value.forEach(item => {
    if (item.product && item.product.price) {
      sum += item.product.price * item.num
    }
  })
  return sum.toFixed(2)
})

// 加载购物车列表
const getCartList = async () => {
  try {
    const res = await request.get('/cart/list')
    console.log('购物车接口返回数据', res.data) // 控制台打印调试
    if (res.code === 200) {
      cartList.value = res.data
    }
  } catch (err) {
    ElMessage.error('加载购物车失败，请重新登录')
    console.error(err)
  }
}

// 修改数量
const changeNum = async (id, num) => {
  await request.put('/cart/num', null, { params: { id, num } })
  ElMessage.success('数量已更新')
  getCartList()
}

// 删除单项
const delCart = (id) => {
  ElMessageBox.confirm('确定要移除该商品？', '提示').then(async () => {
    await request.delete(`/cart/${id}`)
    ElMessage.success('已删除')
    getCartList()
  })
}

// 清空全部
const clearAllCart = () => {
  ElMessageBox.confirm('确定清空全部购物车？', '提示').then(async () => {
    await request.delete('/cart/clear')
    ElMessage.success('已清空')
    getCartList()
  })
}

onMounted(() => getCartList())
</script>

<style scoped>
.cart-wrap {
  font-size:15px;
}
</style>