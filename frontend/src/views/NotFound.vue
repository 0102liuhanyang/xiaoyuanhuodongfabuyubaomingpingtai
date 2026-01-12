<template>
  <div class="page">
    <el-result icon="info" title="404" sub-title="页面不存在">
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.page {
  min-height: 60vh;
  display: grid;
  place-items: center;
}
</style>
