<template>
  <!-- 通用数据表格组件 -->
  <div class="data-table">
    <!-- 表格工具栏 -->
    <div v-if="showToolbar" class="data-table__toolbar">
      <div class="data-table__toolbar-left">
        <slot name="toolbar-left">
          <el-button 
            v-if="showAdd" 
            type="primary" 
            :icon="Plus" 
            @click="handleAdd"
          >
            {{ addText }}
          </el-button>
          <el-button 
            v-if="showBatchDelete && hasSelection" 
            type="danger" 
            :icon="Delete" 
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
          <el-button 
            v-if="showExport" 
            :icon="Download" 
            @click="handleExport"
          >
            导出
          </el-button>
          <el-button 
            v-if="showRefresh" 
            :icon="Refresh" 
            @click="handleRefresh"
          >
            刷新
          </el-button>
        </slot>
      </div>
      
      <div class="data-table__toolbar-right">
        <slot name="toolbar-right">
          <!-- 列设置 -->
          <el-dropdown v-if="showColumnSetting" @command="handleColumnCommand">
            <el-button :icon="Setting">
              列设置
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item 
                  v-for="col in configurableColumns" 
                  :key="col.prop"
                  :command="{ type: 'toggle', column: col }"
                >
                  <el-checkbox 
                    :model-value="!col.hidden" 
                    @change="toggleColumn(col)"
                  >
                    {{ col.label }}
                  </el-checkbox>
                </el-dropdown-item>
                <el-divider style="margin: 5px 0;" />
                <el-dropdown-item :command="{ type: 'reset' }">
                  重置列
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <!-- 密度设置 -->
          <el-dropdown v-if="showDensity" @command="handleDensityCommand">
            <el-button :icon="More">
              密度
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="large">宽松</el-dropdown-item>
                <el-dropdown-item command="default">默认</el-dropdown-item>
                <el-dropdown-item command="small">紧凑</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </slot>
      </div>
    </div>

    <!-- 数据表格 -->
    <el-table
      ref="tableRef"
      v-loading="loading"
      :data="tableData"
      :height="height"
      :max-height="maxHeight"
      :size="tableSize"
      :border="border"
      :stripe="stripe"
      :show-header="showHeader"
      :highlight-current-row="highlightCurrentRow"
      :row-class-name="rowClassName"
      :cell-class-name="cellClassName"
      :empty-text="emptyText"
      :default-sort="defaultSort"
      @selection-change="handleSelectionChange"
      @current-change="handleCurrentChange"
      @sort-change="handleSortChange"
      @row-click="handleRowClick"
      @row-dblclick="handleRowDbClick"
    >
      <!-- 选择列 -->
      <el-table-column
        v-if="showSelection"
        type="selection"
        width="50"
        align="center"
        fixed="left"
      />
      
      <!-- 序号列 -->
      <el-table-column
        v-if="showIndex"
        type="index"
        label="序号"
        width="60"
        align="center"
        fixed="left"
        :index="indexMethod"
      />

      <!-- 数据列 -->
      <template v-for="column in visibleColumns" :key="column.prop">
        <el-table-column
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :fixed="column.fixed"
          :align="column.align || 'left'"
          :sortable="column.sortable"
          :show-overflow-tooltip="column.showOverflowTooltip !== false"
          :class-name="column.className"
        >
          <template #default="{ row, column: col, $index }">
            <slot 
              :name="column.prop" 
              :row="row" 
              :column="col" 
              :index="$index"
              :value="getColumnValue(row, column.prop)"
            >
              <!-- 默认渲染 -->
              <template v-if="column.type === 'image'">
                <el-image
                  :src="getColumnValue(row, column.prop)"
                  :preview-src-list="[getColumnValue(row, column.prop)]"
                  :preview-teleported="true"
                  style="width: 40px; height: 40px;"
                  fit="cover"
                />
              </template>
              <template v-else-if="column.type === 'tag'">
                <el-tag
                  :type="getTagType(getColumnValue(row, column.prop), column.tagMap)"
                  :effect="column.tagEffect || 'light'"
                >
                  {{ getTagText(getColumnValue(row, column.prop), column.tagMap) }}
                </el-tag>
              </template>
              <template v-else-if="column.type === 'switch'">
                <el-switch
                  :model-value="getColumnValue(row, column.prop)"
                  @change="(val) => handleSwitchChange(row, column.prop, val)"
                />
              </template>
              <template v-else-if="column.type === 'date'">
                {{ formatDate(getColumnValue(row, column.prop), column.dateFormat) }}
              </template>
              <template v-else-if="column.type === 'currency'">
                ¥{{ formatCurrency(getColumnValue(row, column.prop)) }}
              </template>
              <template v-else>
                {{ getColumnValue(row, column.prop) }}
              </template>
            </slot>
          </template>
        </el-table-column>
      </template>

      <!-- 操作列 -->
      <el-table-column
        v-if="showActions"
        label="操作"
        :width="actionWidth"
        :min-width="actionMinWidth"
        fixed="right"
        align="center"
      >
        <template #default="{ row, $index }">
          <slot name="actions" :row="row" :index="$index">
            <el-button
              v-if="showEdit"
              type="primary"
              size="small"
              link
              @click="handleEdit(row, $index)"
            >
              编辑
            </el-button>
            <el-button
              v-if="showDelete"
              type="danger"
              size="small"
              link
              @click="handleDelete(row, $index)"
            >
              删除
            </el-button>
          </slot>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页器 -->
    <div v-if="showPagination" class="data-table__pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="pageSizes"
        :layout="paginationLayout"
        :background="paginationBackground"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { ElTable, ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Download, Refresh, Setting, More } from '@element-plus/icons-vue'
import { formatDate as utilFormatDate, formatCurrency as utilFormatCurrency } from '@/utils/format'

/**
 * 表格列配置接口
 */
export interface TableColumn {
  prop: string
  label: string
  width?: string | number
  minWidth?: string | number
  fixed?: boolean | 'left' | 'right'
  align?: 'left' | 'center' | 'right'
  sortable?: boolean | string
  showOverflowTooltip?: boolean
  className?: string
  type?: 'text' | 'image' | 'tag' | 'switch' | 'date' | 'currency'
  dateFormat?: string
  tagMap?: Record<string, { text: string; type: string }>
  tagEffect?: 'dark' | 'light' | 'plain'
  hidden?: boolean
}

/**
 * 组件Props
 */
interface Props {
  // 数据相关
  data?: any[]
  loading?: boolean
  
  // 表格配置
  columns: TableColumn[]
  height?: string | number
  maxHeight?: string | number
  border?: boolean
  stripe?: boolean
  showHeader?: boolean
  highlightCurrentRow?: boolean
  rowClassName?: string | ((row: any, index: number) => string)
  cellClassName?: string | ((row: any, column: any, rowIndex: number, columnIndex: number) => string)
  emptyText?: string
  defaultSort?: { prop: string; order: string }
  
  // 功能开关
  showToolbar?: boolean
  showSelection?: boolean
  showIndex?: boolean
  showActions?: boolean
  showAdd?: boolean
  showBatchDelete?: boolean
  showExport?: boolean
  showRefresh?: boolean
  showEdit?: boolean
  showDelete?: boolean
  showColumnSetting?: boolean
  showDensity?: boolean
  
  // 操作列配置
  actionWidth?: string | number
  actionMinWidth?: string | number
  
  // 工具栏文案
  addText?: string
  
  // 分页配置
  showPagination?: boolean
  total?: number
  currentPage?: number
  pageSize?: number
  pageSizes?: number[]
  paginationLayout?: string
  paginationBackground?: boolean
  
  // 表格尺寸
  size?: 'large' | 'default' | 'small'
}

const props = withDefaults(defineProps<Props>(), {
  data: () => [],
  loading: false,
  border: true,
  stripe: true,
  showHeader: true,
  highlightCurrentRow: false,
  emptyText: '暂无数据',
  showToolbar: true,
  showSelection: false,
  showIndex: true,
  showActions: true,
  showAdd: true,
  showBatchDelete: true,
  showExport: false,
  showRefresh: true,
  showEdit: true,
  showDelete: true,
  showColumnSetting: true,
  showDensity: true,
  actionWidth: 120,
  actionMinWidth: 120,
  addText: '新增',
  showPagination: true,
  total: 0,
  currentPage: 1,
  pageSize: 20,
  pageSizes: () => [10, 20, 50, 100],
  paginationLayout: 'total, sizes, prev, pager, next, jumper',
  paginationBackground: true,
  size: 'default'
})

/**
 * 组件Emits
 */
interface Emits {
  'update:currentPage': [page: number]
  'update:pageSize': [size: number]
  'selection-change': [selection: any[]]
  'current-change': [currentRow: any, oldCurrentRow: any]
  'sort-change': [sort: { column: any; prop: string; order: string }]
  'row-click': [row: any, column: any, event: Event]
  'row-dblclick': [row: any, column: any, event: Event]
  'add': []
  'edit': [row: any, index: number]
  'delete': [row: any, index: number]
  'batch-delete': [selection: any[]]
  'export': []
  'refresh': []
  'switch-change': [row: any, prop: string, value: boolean]
}

const emit = defineEmits<Emits>()

// 模板引用
const tableRef = ref<InstanceType<typeof ElTable>>()

// 响应式数据
const tableData = computed(() => props.data)
const tableSize = ref(props.size)
const selectedRows = ref<any[]>([])
const columnsConfig = ref([...props.columns])

// 计算属性
const hasSelection = computed(() => selectedRows.value.length > 0)

const configurableColumns = computed(() => 
  columnsConfig.value.filter(col => col.prop !== 'selection' && col.prop !== 'index')
)

const visibleColumns = computed(() => 
  columnsConfig.value.filter(col => !col.hidden)
)

// 分页相关
const currentPage = ref(props.currentPage)
const pageSize = ref(props.pageSize)

// 监听分页变化
watch(() => props.currentPage, (val) => {
  currentPage.value = val
})

watch(() => props.pageSize, (val) => {
  pageSize.value = val
})

// 序号方法
const indexMethod = (index: number) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

// 获取列值
const getColumnValue = (row: any, prop: string) => {
  return prop.split('.').reduce((obj, key) => obj?.[key], row)
}

// 标签相关方法
const getTagType = (value: any, tagMap?: Record<string, { text: string; type: string }>) => {
  return tagMap?.[value]?.type || 'info'
}

const getTagText = (value: any, tagMap?: Record<string, { text: string; type: string }>) => {
  return tagMap?.[value]?.text || value
}

// 格式化方法
const formatDate = (date: any, format = 'YYYY-MM-DD HH:mm:ss') => {
  return date ? utilFormatDate(date, format) : '-'
}

const formatCurrency = (amount: any) => {
  return utilFormatCurrency(amount)
}

// 事件处理
const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
  emit('selection-change', selection)
}

const handleCurrentChange = (currentRow: any, oldCurrentRow: any) => {
  emit('current-change', currentRow, oldCurrentRow)
}

const handleSortChange = (sort: { column: any; prop: string; order: string }) => {
  emit('sort-change', sort)
}

const handleRowClick = (row: any, column: any, event: Event) => {
  emit('row-click', row, column, event)
}

const handleRowDbClick = (row: any, column: any, event: Event) => {
  emit('row-dblclick', row, column, event)
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  emit('update:currentPage', page)
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  emit('update:pageSize', size)
}

const handleAdd = () => {
  emit('add')
}

const handleEdit = (row: any, index: number) => {
  emit('edit', row, index)
}

const handleDelete = async (row: any, index: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    emit('delete', row, index)
  } catch {
    // 用户取消
  }
}

const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的数据')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条记录吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    emit('batch-delete', selectedRows.value)
  } catch {
    // 用户取消
  }
}

const handleExport = () => {
  emit('export')
}

const handleRefresh = () => {
  emit('refresh')
}

const handleSwitchChange = (row: any, prop: string, value: boolean) => {
  emit('switch-change', row, prop, value)
}

// 列设置相关
const handleColumnCommand = (command: any) => {
  if (command.type === 'reset') {
    resetColumns()
  }
}

const toggleColumn = (column: TableColumn) => {
  column.hidden = !column.hidden
}

const resetColumns = () => {
  columnsConfig.value = [...props.columns]
}

// 密度设置
const handleDensityCommand = (command: string) => {
  tableSize.value = command as 'large' | 'default' | 'small'
}

// 公共方法
const clearSelection = () => {
  tableRef.value?.clearSelection()
}

const toggleRowSelection = (row: any, selected?: boolean) => {
  tableRef.value?.toggleRowSelection(row, selected)
}

const setCurrentRow = (row: any) => {
  tableRef.value?.setCurrentRow(row)
}

const scrollTo = (options: ScrollToOptions) => {
  nextTick(() => {
    tableRef.value?.scrollTo(options)
  })
}

// 暴露方法
defineExpose({
  clearSelection,
  toggleRowSelection,
  setCurrentRow,
  scrollTo,
  tableRef
})
</script>

<style lang="scss" scoped>
.data-table {
  &__toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px;
    background: var(--el-bg-color-page);
    border-radius: 8px;

    &-left,
    &-right {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
    padding: 16px 0;
  }

  // 表格样式调整
  :deep(.el-table) {
    .el-table__header-wrapper {
      background: var(--el-bg-color-page);
    }

    .el-table__body-wrapper {
      .el-table__row {
        transition: background-color 0.2s;

        &:hover {
          background-color: var(--el-fill-color-light);
        }
      }
    }

    // 操作列按钮样式
    .el-button + .el-button {
      margin-left: 8px;
    }
  }

  // 响应式适配
  @media (max-width: 768px) {
    &__toolbar {
      flex-direction: column;
      gap: 12px;

      &-left,
      &-right {
        width: 100%;
        justify-content: center;
      }
    }

    &__pagination {
      justify-content: center;
    }
  }
}
</style>