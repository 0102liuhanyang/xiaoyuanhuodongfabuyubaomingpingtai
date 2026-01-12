import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, BarChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'

use([CanvasRenderer, PieChart, BarChart, TooltipComponent, LegendComponent, GridComponent])

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
