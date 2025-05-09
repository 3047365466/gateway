// Axios Configuration
axios.defaults.baseURL = '/';
axios.defaults.headers.common['Content-Type'] = 'application/json';

// Import Components
// ServiceManagement and RuleManagement components would be imported here in a production app
// For direct script tags in HTML, they need to be loaded before this file

// Components
const App = {
    template: `
        <div class="main-container">
            <div class="sidebar">
                <div class="logo">
                    <span>API网关系统</span>
                </div>
                <div class="sidebar-menu">
                    <div class="sidebar-item" :class="{ active: $route.path === '/' }" @click="$router.push('/')">
                        <i class="el-icon-s-home"></i>
                        <span>主页</span>
                    </div>
                    <div class="sidebar-item" :class="{ active: $route.path.includes('/service') }" @click="$router.push('/service')">
                        <i class="el-icon-s-grid"></i>
                        <span>服务实例管理</span>
                    </div>
                    <div class="sidebar-item" :class="{ active: $route.path.includes('/rule') }" @click="$router.push('/rule')">
                        <i class="el-icon-s-operation"></i>
                        <span>请求规则管理</span>
                    </div>
                </div>
            </div>
            <div class="content-container">
                <router-view></router-view>
            </div>
        </div>
    `
};

// Dashboard Component
const Dashboard = {
    data() {
        return {
            stats: {
                serviceCount: 0,
                instanceCount: 0,
                ruleCount: 0
            }
        };
    },
    template: `
        <div>
            <div class="header-info">
                <h2>服务注册实例数：{{ stats.serviceCount }}</h2>
            </div>
            <div class="main-content">
                <el-row :gutter="20">
                    <el-col :span="8">
                        <el-card class="stats-card">
                            <div slot="header">服务注册实例数</div>
                            <div>
                                <h3>{{ stats.serviceCount }}</h3>
                            </div>
                        </el-card>
                    </el-col>
                    <el-col :span="8">
                        <el-card class="stats-card">
                            <div slot="header">节点实例数</div>
                            <div>
                                <h3>{{ stats.instanceCount }}</h3>
                            </div>
                        </el-card>
                    </el-col>
                    <el-col :span="8">
                        <el-card class="stats-card">
                            <div slot="header">规则数</div>
                            <div>
                                <h3>{{ stats.ruleCount }}</h3>
                            </div>
                        </el-card>
                    </el-col>
                </el-row>
            </div>
        </div>
    `,
    created() {
        this.fetchData();
    },
    methods: {
        async fetchData() {
            try {
                const serviceRes = await axios.get('/serviceDefinition/getList?prefixPath=edanRapid-dev');
                this.stats.serviceCount = serviceRes.data.length || 0;
                
                // Get instance count by combining all service instances
                let instanceCount = 0;
                for (const service of serviceRes.data) {
                    const instanceRes = await axios.get(`/serviceInstance/getList?prefixPath=edanRapid-dev&uniqueId=${service.uniqueId}`);
                    instanceCount += instanceRes.data.length || 0;
                }
                this.stats.instanceCount = instanceCount;
                
                const ruleRes = await axios.get('/rule/getList?prefixPath=edanRapid-dev');
                this.stats.ruleCount = ruleRes.data.length || 0;
            } catch (error) {
                console.error('Error fetching dashboard data:', error);
                this.$message.error('获取仪表盘数据失败');
            }
        }
    }
};

// Router Configuration
const routes = [
    { path: '/', component: Dashboard },
    { path: '/service', component: ServiceManagement },
    { path: '/rule', component: RuleManagement }
];

const router = new VueRouter({
    routes
});

// Vue Application
new Vue({
    el: '#app',
    router,
    render: h => h(App)
}); 