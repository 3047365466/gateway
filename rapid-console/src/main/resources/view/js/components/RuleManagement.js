// Rule Management Component
const RuleManagement = {
    data() {
        return {
            prefixPath: 'edanRapid-dev',
            rules: [],
            dialogVisible: false,
            filterDialogVisible: false,
            isCreating: false,
            currentRule: null,
            ruleForm: {
                id: '',
                name: '',
                protocol: 'HTTP',
                order: 0,
                filterConfigs: []
            },
            filterForm: {
                filterType: 'timeoutPreFilter',
                config: ''
            },
            filterTypes: [
                { value: 'timeoutPreFilter', label: 'timeoutPreFilter' },
                { value: 'loadBalancePreFilter', label: 'loadBalancePreFilter' },
                { value: 'limitPreFilter', label: 'limitPreFilter' }
            ]
        };
    },
    template: `
        <div>
            <div class="header-info">
                <h2>请求规则管理</h2>
            </div>
            <div class="main-content">
                <div class="filter-container">
                    <el-button type="primary" @click="showCreateRuleDialog">新增</el-button>
                </div>
                
                <el-table
                    :data="rules"
                    border
                    style="width: 100%">
                    <el-table-column prop="id" label="规则名称"></el-table-column>
                    <el-table-column prop="protocol" label="协议"></el-table-column>
                    <el-table-column prop="order" label="优先级"></el-table-column>
                    <el-table-column label="NineConfigs">
                        <template slot-scope="scope">
                            <span>{{ scope.row.filterConfigs ? '...' : '-' }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="映射服务接口">
                        <template slot-scope="scope">
                            <span>/bi-etl-task/start</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="220">
                        <template slot-scope="scope">
                            <el-button @click="showEditRuleDialog(scope.row)" type="text" size="small">编辑</el-button>
                            <el-button @click="deleteRule(scope.row)" type="text" size="small">删除</el-button>
                            <el-button @click="showFilterDialog(scope.row)" type="text" size="small">查看过滤器</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            
            <!-- 规则编辑对话框 -->
            <el-dialog
                :title="isCreating ? '新增请求规则' : '编辑请求规则'"
                :visible.sync="dialogVisible"
                width="40%">
                <el-form :model="ruleForm" label-width="100px">
                    <el-form-item label="规则名称">
                        <el-input v-model="ruleForm.name" placeholder="请输入"></el-input>
                    </el-form-item>
                    <el-form-item label="protocol">
                        <el-input v-model="ruleForm.protocol" placeholder="请输入"></el-input>
                    </el-form-item>
                    <el-form-item label="前置过滤器">
                        <el-select v-model="filterForm.filterType" multiple placeholder="默认标签" clearable style="width: 100%">
                            <el-option
                                v-for="item in filterTypes"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value">
                            </el-option>
                        </el-select>
                        <div style="margin-top: 10px;">
                            <span>config</span>
                            <el-input type="textarea" v-model="filterForm.config" placeholder="需要于前面过滤器数量一致"></el-input>
                        </div>
                    </el-form-item>
                    <el-form-item label="后置过滤器">
                        <el-select v-model="filterForm.filterType" multiple placeholder="默认标签" clearable style="width: 100%">
                            <el-option
                                v-for="item in filterTypes"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value">
                            </el-option>
                        </el-select>
                        <div style="margin-top: 10px;">
                            <span>config</span>
                            <el-input type="textarea" v-model="filterForm.config" placeholder="需要于前面过滤器数量一致"></el-input>
                        </div>
                    </el-form-item>
                    <el-form-item label="优先级">
                        <el-input v-model.number="ruleForm.order" placeholder="请输入"></el-input>
                    </el-form-item>
                </el-form>
                <span slot="footer" class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="saveRule">确认</el-button>
                </span>
            </el-dialog>
            
            <!-- 过滤器对话框 -->
            <el-dialog
                title="过滤器配置"
                :visible.sync="filterDialogVisible"
                width="50%">
                <div v-if="currentRule && currentRule.filterConfigs">
                    <h4>前置过滤器</h4>
                    <el-table :data="getFiltersByType('pre')" border>
                        <el-table-column prop="id" label="过滤器ID"></el-table-column>
                        <el-table-column prop="config" label="配置"></el-table-column>
                    </el-table>
                    
                    <h4 style="margin-top: 20px;">后置过滤器</h4>
                    <el-table :data="getFiltersByType('post')" border>
                        <el-table-column prop="id" label="过滤器ID"></el-table-column>
                        <el-table-column prop="config" label="配置"></el-table-column>
                    </el-table>
                </div>
                <div v-else>
                    <p>没有配置过滤器</p>
                </div>
            </el-dialog>
        </div>
    `,
    created() {
        this.fetchRules();
    },
    methods: {
        async fetchRules() {
            try {
                const res = await axios.get(`/rule/getList?prefixPath=${this.prefixPath}`);
                this.rules = res.data || [];
            } catch (error) {
                console.error('Error fetching rules:', error);
                this.$message.error('获取规则列表失败');
            }
        },
        showCreateRuleDialog() {
            this.isCreating = true;
            this.ruleForm = {
                id: '',
                name: '',
                protocol: 'HTTP',
                order: 0,
                filterConfigs: []
            };
            this.dialogVisible = true;
        },
        showEditRuleDialog(rule) {
            this.isCreating = false;
            this.ruleForm = {
                id: rule.id,
                name: rule.name,
                protocol: rule.protocol,
                order: rule.order,
                filterConfigs: rule.filterConfigs || []
            };
            this.dialogVisible = true;
        },
        showFilterDialog(rule) {
            this.currentRule = rule;
            this.filterDialogVisible = true;
        },
        getFiltersByType(type) {
            if (!this.currentRule || !this.currentRule.filterConfigs) return [];
            
            return this.currentRule.filterConfigs.filter(filter => {
                if (type === 'pre') {
                    return filter.id.toLowerCase().includes('prefilter');
                } else if (type === 'post') {
                    return filter.id.toLowerCase().includes('postfilter');
                }
                return false;
            });
        },
        async saveRule() {
            try {
                const ruleData = {
                    prefixPath: this.prefixPath,
                    id: this.ruleForm.id,
                    name: this.ruleForm.name,
                    protocol: this.ruleForm.protocol,
                    order: this.ruleForm.order,
                    filterConfigs: this.ruleForm.filterConfigs
                };

                if (this.isCreating) {
                    await axios.post('/rule/add', ruleData);
                    this.$message.success('创建规则成功');
                } else {
                    await axios.post('/rule/update', ruleData);
                    this.$message.success('更新规则成功');
                }
                
                this.dialogVisible = false;
                this.fetchRules();
            } catch (error) {
                console.error('Error saving rule:', error);
                this.$message.error('保存规则失败');
            }
        },
        async deleteRule(rule) {
            try {
                await this.$confirm('确认删除该规则吗?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                });
                
                await axios.post('/rule/delete', {
                    prefixPath: this.prefixPath,
                    id: rule.id
                });
                
                this.$message.success('删除规则成功');
                this.fetchRules();
            } catch (error) {
                if (error !== 'cancel') {
                    console.error('Error deleting rule:', error);
                    this.$message.error('删除规则失败');
                }
            }
        }
    }
}; 