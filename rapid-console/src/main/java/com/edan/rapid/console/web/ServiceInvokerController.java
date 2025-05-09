package com.edan.rapid.console.web;

import com.edan.rapid.common.config.ServiceInvoker;
import com.edan.rapid.console.service.ServiceDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * <B>主类名称：</B>ServiceInvokerController<BR>
 * <B>概要说明：</B>服务定义方法控制层<BR>
 * @author JiFeng
 * @since 2021年11月29日 上午10:41:14
 */
@RestController
public class ServiceInvokerController {

	@Autowired
	private ServiceDefinitionService serviceDefinitionService;
	
	@RequestMapping("/serviceInvoker/getListByUniqueId")
	public List<ServiceInvoker> getListByUniqueId(@RequestParam("prefixPath")String prefixPath, 
			@RequestParam("uniqueId")String uniqueId) throws Exception{
		List<ServiceInvoker> list = serviceDefinitionService.getServiceInvokerByUniqueId(prefixPath, uniqueId);
		return list;
	}
	
	@RequestMapping("/serviceInvoker/bindingRuleId")
	public void bindingRuleId(@RequestParam("prefixPath")String prefixPath, 
			@RequestParam("uniqueId")String uniqueId,
			@RequestParam("invokerPath")String invokerPath,
			@RequestParam("ruleId")String ruleId) throws Exception {
		
		serviceDefinitionService.serviceInvokerbindingRuleId(prefixPath, uniqueId, invokerPath, ruleId);
	}
	
}
