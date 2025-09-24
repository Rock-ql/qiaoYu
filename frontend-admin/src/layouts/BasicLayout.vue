<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter, RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { menuConfig } from '@/router/admin'
import { ElMessageBox } from 'element-plus'
import * as Icons from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

// 侧边栏折叠
const collapsed = ref(false)
const activePath = computed(() => route.path)

function iconByName(name?: string) {
  if (!name) return null
  return (Icons as any)[name] || null
}

async function onLogout() {
  const ok = await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', { type: 'warning' }).catch(() => false)
  if (ok) auth.logout()
}
</script>

<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="brand" @click="router.push('/')">
        <span class="logo-text">羽毛球后台</span>
      </div>
      <el-menu :default-active="activePath" class="menu" router :collapse="collapsed">
        <template v-for="item in menuConfig" :key="item.path">
          <el-menu-item :index="item.path">
            <!-- 不展示图标，仅文字，更清爽 -->
            <span class="menu-title">{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="left">
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item to="/dashboard">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-for="(bc, idx) in (route.meta.breadcrumb || [])" :key="idx" :to="bc.path">{{ bc.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="right">
          <el-dropdown>
            <span class="user">
              <el-avatar size="small" :icon="Icons.UserFilled" />
              <span class="name">{{ auth.currentUser?.nickname || auth.currentUser?.username || '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/settings')">个人设置</el-dropdown-item>
                <el-dropdown-item divided @click="onLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <el-card shadow="never" class="content-card">
          <RouterView />
        </el-card>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout { min-height: 100vh; }
.aside { border-right: 1px solid #eef0f4; background: #fff; }
.brand { height: 56px; display:flex; align-items:center; gap:10px; padding:0 12px; font-weight:600; cursor:pointer; }
.logo-text { font-size: 16px; }
.menu { border-right: none; }
.header { height:56px; display:flex; align-items:center; justify-content:space-between; border-bottom:1px solid #eef0f4; }
.left { display:flex; align-items:center; gap:12px; }
.breadcrumb { margin-left: 6px; }
.right .user { display:inline-flex; align-items:center; gap:8px; }
.name { color:#374151; }
.main { background: #f7f8fa; padding: 16px; }
.content-card { min-height: calc(100vh - 56px - 32px); }
.menu-title { margin-left: 4px; }
</style>
