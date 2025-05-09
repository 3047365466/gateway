// API utility functions for Rapid Console

const API = {
    // Default prefix path for all API calls
    prefixPath: 'edanRapid-dev',
    
    // Service Definition APIs
    serviceDefinition: {
        getList: async (prefixPath = API.prefixPath) => {
            try {
                const response = await axios.get(`/serviceDefinition/getList?prefixPath=${prefixPath}`);
                return response.data || [];
            } catch (error) {
                console.error('API Error - serviceDefinition.getList:', error);
                throw error;
            }
        },
        
        updatePatternPath: async (uniqueId, patternPath, prefixPath = API.prefixPath) => {
            try {
                const response = await axios.post('/serviceDefinition/updatePatternPathByUniqueId', {
                    prefixPath,
                    uniqueId,
                    patternPath
                });
                return response.data;
            } catch (error) {
                console.error('API Error - serviceDefinition.updatePatternPath:', error);
                throw error;
            }
        },
        
        updateEnable: async (uniqueId, enable, prefixPath = API.prefixPath) => {
            try {
                const response = await axios.post('/serviceDefinition/updateEnableByUniqueId', {
                    prefixPath,
                    uniqueId,
                    enable
                });
                return response.data;
            } catch (error) {
                console.error('API Error - serviceDefinition.updateEnable:', error);
                throw error;
            }
        }
    },
    
    // Service Instance APIs
    serviceInstance: {
        getList: async (uniqueId, prefixPath = API.prefixPath) => {
            try {
                const response = await axios.get(`/serviceInstance/getList?prefixPath=${prefixPath}&uniqueId=${uniqueId}`);
                return response.data || [];
            } catch (error) {
                console.error('API Error - serviceInstance.getList:', error);
                throw error;
            }
        },
        
        updateEnable: async (uniqueId, serviceInstanceId, enable, prefixPath = API.prefixPath) => {
            try {
                const response = await axios.post('/serviceInstance/updateEnable', {
                    prefixPath,
                    uniqueId,
                    serviceInstanceId,
                    enable
                });
                return response.data;
            } catch (error) {
                console.error('API Error - serviceInstance.updateEnable:', error);
                throw error;
            }
        },
        
        updateTags: async (uniqueId, serviceInstanceId, tags, prefixPath = API.prefixPath) => {
            try {
                const response = await axios.post('/serviceInstance/updateTags', {
                    prefixPath,
                    uniqueId,
                    serviceInstanceId,
                    tags
                });
                return response.data;
            } catch (error) {
                console.error('API Error - serviceInstance.updateTags:', error);
                throw error;
            }
        },
        
        updateWeight: async (uniqueId, serviceInstanceId, weight, prefixPath = API.prefixPath) => {
            try {
                const response = await axios.post('/serviceInstance/updateWeight', {
                    prefixPath,
                    uniqueId,
                    serviceInstanceId,
                    weight
                });
                return response.data;
            } catch (error) {
                console.error('API Error - serviceInstance.updateWeight:', error);
                throw error;
            }
        }
    },
    
    // Rule APIs
    rule: {
        getList: async (prefixPath = API.prefixPath) => {
            try {
                const response = await axios.get(`/rule/getList?prefixPath=${prefixPath}`);
                return response.data || [];
            } catch (error) {
                console.error('API Error - rule.getList:', error);
                throw error;
            }
        },
        
        add: async (rule, prefixPath = API.prefixPath) => {
            try {
                const response = await axios.post('/rule/add', {
                    prefixPath,
                    ...rule
                });
                return response.data;
            } catch (error) {
                console.error('API Error - rule.add:', error);
                throw error;
            }
        },
        
        update: async (rule, prefixPath = API.prefixPath) => {
            try {
                const response = await axios.post('/rule/update', {
                    prefixPath,
                    ...rule
                });
                return response.data;
            } catch (error) {
                console.error('API Error - rule.update:', error);
                throw error;
            }
        },
        
        delete: async (id, prefixPath = API.prefixPath) => {
            try {
                const response = await axios.post('/rule/delete', {
                    prefixPath,
                    id
                });
                return response.data;
            } catch (error) {
                console.error('API Error - rule.delete:', error);
                throw error;
            }
        }
    }
}; 