<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="page">
    <el-result icon="warning" title="403" sub-title="没有权限访问该页面">
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
