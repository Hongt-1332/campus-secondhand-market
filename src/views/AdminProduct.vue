<template>
  <div style="padding:20px;max-width:1400px;margin:0 auto;">
    <h2>商品管理</h2>
    <el-card style="margin:15px 0;">
      <el-form inline :model="search">
        <el-form-item label="商品名称">
          <el-input v-model="search.name" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">查询</el-button>
          <el-button @click="search={};getList()">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column label="ID" prop="id" width="70"></el-table-column>
      <el-table-column label="商品名称" prop="name"></el-table-column>
      <el-table-column label="描述" prop="desc"></el-table-column>
      <el-table-column label="价格" prop="price"></el-table-column>
      <el-table-column label="分类" prop="category"></el-table-column>
      <el-table-column label="卖家" prop="seller"></el-table-column>
      <el-table-column label="联系方式" prop="phone"></el-table-column>
      <el-table-column label="发布时间" prop="createTime"></el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top:15px;text-align:right"
      @change="getList"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const search = ref({ name:'' })

const getList = async () => {
  loading.value = true
  const res = await request.get('/product/list', {
    params: {
      name: search.value.name,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  })
  loading.value = false
  if(res.code === 200){
    tableData.value = res.data
    total.value = res.data.length
  }
}

onMounted(()=>getList())
</script>