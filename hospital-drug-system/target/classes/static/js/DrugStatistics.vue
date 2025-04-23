<template>
  <div class="statistics-container">
    <h1 class="page-title">药品统计</h1>
    
    <el-card class="chart-card">
      <div id="category-chart" ref="categoryChart" class="chart"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册 ECharts 必需的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  BarChart,
  CanvasRenderer
])

const categoryChart = ref(null)
const dateRange = ref([])
let chart = null

// 页面载入时加载数据
onMounted(() => {
  // 设置默认查询时间范围为过去7天
  // dateRange.value = [ // dateRange functionality might need review later
  //   new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
  //   new Date()
  // ]
  // fetchDrugCategories() // Removed undefined function call
  // fetchDrugList() // Removed undefined function call
  // fetchTransactionData() // Removed undefined function call
  
  // 初始化图表
  initChart()
  
  // 加载数据 (Keep the defined function)
  fetchCategoryData()
  
  // 响应式调整图表大小
  window.addEventListener('resize', () => {
    if (chart) {
      chart.resize()
    }
  })
})

// 初始化图表
const initChart = () => {
  if (categoryChart.value) {
    chart = echarts.init(categoryChart.value)
    
    // 设置初始加载状态
    chart.showLoading()
  }
}

// 获取药品类别数据
const fetchCategoryData = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/statistics/category', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    // 更新图表
    renderChart(response.data)
  } catch (error) {
    console.error('获取药品分类统计数据失败:', error)
    ElMessage.error('获取药品分类统计数据失败')
  }
}

// 渲染图表
const renderChart = (data) => {
  if (!chart) return
  
  // 隐藏加载状态
  chart.hideLoading()
  
  // 如果没有数据
  if (!data || data.length === 0) {
    chart.setOption({
      title: {
        text: '暂无药品类别数据',
        left: 'center',
        top: 'center',
        textStyle: {
          fontSize: 16,
          color: '#999'
        }
      }
    })
    return
  }
  
  // 准备图表数据
  const categories = data.map(item => item.category)
  const counts = data.map(item => item.count)
  
  // 设置图表配置
  const option = {
    title: {
      text: '药品类别分布统计',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      name: '数量'
    },
    series: [
      {
        name: '药品数量',
        type: 'bar',
        data: counts,
        itemStyle: {
          color: function(params) {
            // 颜色列表
            const colorList = [
              '#5470c6', '#91cc75', '#fac858', '#ee6666',
              '#73c0de', '#3ba272', '#fc8452', '#9a60b4'
            ]
            // 循环使用颜色列表
            return colorList[params.dataIndex % colorList.length]
          }
        },
        label: {
          show: true,
          position: 'top'
        }
      }
    ]
  }
  
  // 应用配置
  chart.setOption(option)
}
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.chart-card {
  margin-bottom: 20px;
}

.chart {
  height: 500px;
  width: 100%;
}
</style> 