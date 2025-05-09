// Service Management Component
const ServiceManagement = {
    data() {
        return {
            prefixPath: 'edanRapid-dev',
            services: [],
            currentService: null,
            serviceInstances: [],
            dialogVisible: false,
            editMode: 'weight', // 'weight' or 'tags'
            editForm: {
                weight: 100,
                tags: '',
                instanceId: '',
                uniqueId: ''
            }
        };
    },
    template: `
        <div>
            <div class="header-info">
                <h2>服务实例管理</h2>
            </div>
            <div class="main-content">
                <div class="filter-container">
                    <el-row :gutter="20">
                        <el-col :span="8">
                            <el-select v-model="currentService" placeholder="选择服务" @change="fetchServiceInstances" style="width: 100%">
                                <el-option
                                    v-for="item in services"
                                    :key="item.uniqueId"
                                    :label="item.uniqueId"
                                    :value="item.uniqueId">
                                </el-option>
                            </el-select>
                        </el-col>
                    </el-row>
                </div>
                
                <el-table
                    v-if="currentService"
                    :data="serviceInstances"
                    border
                    style="width: 100%">
                    <el-table-column prop="serviceInstanceId" label="服务实例ID">
                        <template slot-scope="scope">
                            <span>{{ scope.row.ip + ':' + scope.row.port }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="tags" label="tags标签">
                        <template slot-scope="scope">
                            <span>{{ scope.row.tags || '-' }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="weight" label="weight">
                        <template slot-scope="scope">
                            <span>{{ scope.row.weight || 100 }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="registerTime" label="服务启动时间">
                        <template slot-scope="scope">
                            <span>{{ scope.row.registerTime || '-' }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="enable" label="是否可用">
                        <template slot-scope="scope">
                            <el-switch
                                v-model="scope.row.enable"
                                @change="toggleEnable(scope.row)">
                            </el-switch>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="180">
                        <template slot-scope="scope">
                            <el-button @click="editInstance(scope.row, 'weight')" type="text" size="small">修改权重</el-button>
                            <el-button @click="editInstance(scope.row, 'tags')" type="text" size="small">修改标签</el-button>
                        </template>
                    </el-table-column>
                </el-table>
                
                <div v-if="!currentService" style="text-align: center; margin-top: 50px;">
                    <h3>请选择服务实例</h3>
                </div>
            </div>
            
            <!-- 节点信息编辑对话框 -->
            <el-dialog
                :title="editMode === 'weight' ? '节点信息编辑' : '标签编辑'"
                :visible.sync="dialogVisible"
                width="30%">
                <div v-if="editMode === 'weight'">
                    <el-form label-width="80px">
                        <el-form-item label="weight">
                            <el-input-number v-model="editForm.weight" :min="0" :max="200"></el-input-number>
                        </el-form-item>
                    </el-form>
                </div>
                <div v-else-if="editMode === 'tags'">
                    <el-form label-width="80px">
                        <el-form-item label="tags标签">
                            <el-input v-model="editForm.tags" placeholder="分割"></el-input>
                        </el-form-item>
                    </el-form>
                </div>
                <span slot="footer" class="dialog-footer">
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="saveInstanceEdit">确认</el-button>
                </span>
            </el-dialog>
        </div>
    `,
    created() {
        this.fetchServices();
    },
    methods: {
        async fetchServices() {
            try {
                const res = await axios.get(`/serviceDefinition/getList?prefixPath=${this.prefixPath}`);
                this.services = res.data || [];
                if (this.services.length > 0 && !this.currentService) {
                    this.currentService = this.services[0].uniqueId;
                    this.fetchServiceInstances();
                }
            } catch (error) {
                console.error('Error fetching services:', error);
                this.$message.error('获取服务列表失败');
            }
        },
        async fetchServiceInstances() {
            if (!this.currentService) return;
            
            try {
                const res = await axios.get(`/serviceInstance/getList?prefixPath=${this.prefixPath}&uniqueId=${this.currentService}`);
                this.serviceInstances = res.data || [];
            } catch (error) {
                console.error('Error fetching service instances:', error);
                this.$message.error('获取服务实例列表失败');
            }
        },
        async toggleEnable(instance) {
            try {
                await axios.post('/serviceInstance/updateEnable', {
                    prefixPath: this.prefixPath,
                    uniqueId: this.currentService,
                    serviceInstanceId: instance.ip + ':' + instance.port,
                    enable: instance.enable
                });
                this.$message.success('修改服务实例状态成功');
            } catch (error) {
                console.error('Error updating instance enable state:', error);
                this.$message.error('修改服务实例状态失败');
                // Revert UI state
                instance.enable = !instance.enable;
            }
        },
        editInstance(instance, mode) {
            this.editMode = mode;
            this.editForm = {
                instanceId: instance.ip + ':' + instance.port,
                uniqueId: this.currentService,
                weight: instance.weight || 100,
                tags: instance.tags || ''
            };
            this.dialogVisible = true;
        },
        async saveInstanceEdit() {
            try {
                if (this.editMode === 'weight') {
                    await axios.post('/serviceInstance/updateWeight', {
                        prefixPath: this.prefixPath,
                        uniqueId: this.editForm.uniqueId,
                        serviceInstanceId: this.editForm.instanceId,
                        weight: this.editForm.weight
                    });
                } else if (this.editMode === 'tags') {
                    await axios.post('/serviceInstance/updateTags', {
                        prefixPath: this.prefixPath,
                        uniqueId: this.editForm.uniqueId,
                        serviceInstanceId: this.editForm.instanceId,
                        tags: this.editForm.tags
                    });
                }
                
                this.dialogVisible = false;
                this.$message.success('修改成功');
                this.fetchServiceInstances();
            } catch (error) {
                console.error('Error saving instance edit:', error);
                this.$message.error('修改失败');
            }
        }
    }
}; 