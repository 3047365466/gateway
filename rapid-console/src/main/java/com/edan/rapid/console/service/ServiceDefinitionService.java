package com.edan.rapid.console.service;

import com.edan.rapid.common.config.ServiceDefinition;
import com.edan.rapid.common.config.ServiceInvoker;
import com.edan.rapid.common.util.FastJsonConvertUtil;
import com.edan.rapid.common.util.Pair;
import com.edan.rapid.console.VO.ServiceDefinitionVO;
import com.edan.rapid.discovery.api.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <B>主类名称：</B>ServiceDefinitionService<BR>
 * <B>概要说明：</B>ServiceDefinitionService<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午10:52:46
 */
@Service
public class ServiceDefinitionService {

	@Autowired
	private RegistryService registryService;
	
	/**
	 * <B>方法名称：</B>getServiceDefinitionList<BR>
	 * <B>概要说明：</B>根据前缀获取服务定义列表<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 下午10:52:52
	 * @param prefixPath
	 * @return
	 * @throws Exception 
	 */
	public List<ServiceDefinitionVO> getServiceDefinitionList(String prefixPath) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.SERVICE_PREFIX;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		List<ServiceDefinitionVO> serviceDefinitions = new ArrayList<>();
		for(Pair<String, String> pair : list) {
			String p = pair.getObject1();
			if (p.equals(path)) { 
				continue;
			}
			String json = pair.getObject2();
			ServiceDefinitionVO sd = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinitionVO.class);
			serviceDefinitions.add(sd);
		}
		return serviceDefinitions;
	}
	
	public void updatePatternPathByUniqueId(String prefixPath, String uniqueId, String patternPath) throws Exception {
		updateServiceDefinitionByUniqueId(prefixPath, uniqueId, patternPath);
	}
	
	public void updateEnableByUniqueId(String prefixPath, String uniqueId, boolean enable) throws Exception {
		updateServiceDefinitionByUniqueId(prefixPath, uniqueId, enable);
	}

	/**
	 * <B>方法名称：</B>updateServiceDefinitionByUniqueId<BR>
	 * <B>概要说明：</B>根据服务唯一ID 更新patternPath enable<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 下午10:53:36
	 * @param prefixPath
	 * @param uniqueId
	 * @param param
	 * @throws Exception
	 */
	private void updateServiceDefinitionByUniqueId(String prefixPath, String uniqueId, Object param) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.SERVICE_PREFIX
				+ RegistryService.PATH 
				+ uniqueId;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		if(list.size() == 1) {
			Pair<String, String> pair = list.get(0);
			String key = pair.getObject1();
			String json = pair.getObject2();
			ServiceDefinition sd = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
			//	update:  patternPath & enable
			if(param instanceof String) {
				String patternPath = (String)param;
				sd.setPatternPath(patternPath);
			}
			if(param instanceof Boolean) {
				boolean enable = (boolean)param;
				sd.setEnable(enable);
			}
			String value = FastJsonConvertUtil.convertObjectToJSON(sd);
			registryService.registerPersistentNode(key, value);
		}
	}

	/**
	 * <B>方法名称：</B>getServiceInvokerByUniqueId<BR>
	 * <B>概要说明：</B>根据uniqueId获取指定的服务下的调用方法列表<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 下午10:53:18
	 * @param prefixPath
	 * @param uniqueId
	 * @return
	 * @throws Exception
	 */
	public List<ServiceInvoker> getServiceInvokerByUniqueId(String prefixPath, String uniqueId) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.SERVICE_PREFIX
				+ RegistryService.PATH 
				+ uniqueId;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		List<ServiceInvoker> invokerList = new ArrayList<ServiceInvoker>();
		if(list.size() == 1) {
			Pair<String, String> pair = list.get(0);
			String json = pair.getObject2();
			ServiceDefinition sd = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
			Map<String, ServiceInvoker> map = sd.getInvokerMap();
			Iterator<ServiceInvoker> it = map.values().iterator();
			while(it.hasNext()) {
				invokerList.add(it.next());
			}
		}
		return invokerList;
	}

	/**
	 * <B>方法名称：</B>serviceInvokerbindingRuleId<BR>
	 * <B>概要说明：</B>为ServiceInvoker绑定一个规则ID<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 下午10:53:25
	 * @param prefixPath
	 * @param uniqueId
	 * @param invokerPath
	 * @param ruleId
	 * @throws Exception
	 */
	public void serviceInvokerbindingRuleId(String prefixPath, String uniqueId, String invokerPath, String ruleId) throws Exception {
		
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.SERVICE_PREFIX
				+ RegistryService.PATH 
				+ uniqueId;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		if(list.size() == 1) {
			Pair<String, String> pair = list.get(0);
			String key = pair.getObject1();
			String json = pair.getObject2();
			ServiceDefinition sd = FastJsonConvertUtil.convertJSONToObject(json, ServiceDefinition.class);
			Map<String, ServiceInvoker> map = sd.getInvokerMap();
			for(Map.Entry<String, ServiceInvoker> me : map.entrySet()) {
				String pathKey = me.getKey();
				ServiceInvoker invokerValue = me.getValue();
				if(pathKey.equals(invokerPath)) {
					//	绑定ruleId
					invokerValue.setRuleId(ruleId);
				}
			}
			String value = FastJsonConvertUtil.convertObjectToJSON(sd);
			registryService.registerPersistentNode(key, value);
		}
	}
	
}
