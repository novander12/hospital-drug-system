<template>
  <div class="chart-container">
    <h1 class="page-title">库存走势图</h1>
    
    <el-card class="chart-card">
      <div id="stock-chart" ref="stockChart" class="chart"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  ToolboxComponent,
  DataZoomComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册 ECharts 必需的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  ToolboxComponent,
  DataZoomComponent,
  LineChart,
  CanvasRenderer
])

const stockChart = ref(null)
let chart = null

onMounted(() => {
  // 初始化图表
  initChart()
  
  // 加载数据
  fetchStockHistoryData()
  
  // 响应式调整图表大小
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 移除事件监听
  window.removeEventListener('resize', handleResize)
  
  // 销毁图表
  if (chart) {
    chart.dispose()
    chart = null
  }
})

// 处理窗口大小变化
const handleResize = () => {
  if (chart) {
    chart.resize()
  }
}

// 初始化图表
const initChart = () => {
  if (stockChart.value) {
    chart = echarts.init(stockChart.value)
    
    // 设置初始加载状态
    chart.showLoading()
  }
}

// 获取库存历史数据
const fetchStockHistoryData = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/statistics/stock-history?days=30', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    // 更新图表
    renderChart(response.data)
  } catch (error) {
    console.error('获取库存历史数据失败:', error)
    ElMessage.error('获取库存历史数据失败')
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
        text: '暂无库存历史数据',
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
  const dates = data.map(item => item.date)
  const stockValues = data.map(item => item.totalStock)
  
  // 设置图表配置
  const option = {
    title: {
      text: '30天药品库存走势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br />库存总量: {c}'
    },
    toolbox: {
      feature: {
        saveAsImage: {}
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      name: '库存总量'
    },
    dataZoom: [
      {
        type: 'inside',
        start: 0,
        end: 100
      },
      {
        type: 'slider',
        start: 0,
        end: 100
      }
    ],
    series: [
      {
        name: '库存总量',
        type: 'line',
        data: stockValues,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        itemStyle: {
          color: '#409EFF'
        },
        lineStyle: {
          width: 3
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            {
              offset: 0,
              color: 'rgba(64, 158, 255, 0.5)'
            },
            {
              offset: 1,
              color: 'rgba(64, 158, 255, 0.1)'
            }
          ])
        },
        markPoint: {
          data: [
            { type: 'max', name: '最大值' },
            { type: 'min', name: '最小值' }
          ]
        }
      }
    ]
  }
  
  // 应用配置
  chart.setOption(option)
}
</script>

<style scoped>
.chart-container {
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