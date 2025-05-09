package com.edan.rapid.console.web;

import com.edan.rapid.console.VO.ServiceDefinitionVO;
import com.edan.rapid.console.dto.ServiceDefinitionDTO;
import com.edan.rapid.console.service.ServiceDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <B>主类名称：</B>ServiceDefinitionController<BR>
 * <B>概要说明：</B>服务定义控制层<BR>
 * @author JiFeng
 * @since 2021年11月29日 上午10:41:46
 */
@RestController
public class ServiceDefinitionController {

	@Autowired
	private ServiceDefinitionService serviceDefinitionService;
	
	/**
	 * <B>方法名称：</B>getList<BR>
	 * <B>概要说明：</B>获取服务定义列表<BR>
	 * @author JiFeng
	 * @since 2021年11月29日 上午11:04:33
	 * @param prefixPath 前缀路径
	 * @return List<ServiceDefinition>
	 * @throws Exception 
	 */
	@RequestMapping("/serviceDefinition/getList")
	public List<ServiceDefinitionVO> getList(@RequestParam("prefixPath") String prefixPath) throws Exception {
		List<ServiceDefinitionVO> list = serviceDefinitionService.getServiceDefinitionList(prefixPath);
		return list;
	}
	
	/**
	 * <B>方法名称：</B>updatePatternPathByUniqueId<BR>
	 * <B>概要说明：</B>根据uniqueId更新服务定义PatternPath信息<BR>
	 * @author JiFeng
	 * @since 2021年11月29日 上午11:11:25
	 * @param serviceDefinitionDTO
	 * @throws Exception 
	 */
	@RequestMapping("/serviceDefinition/updatePatternPathByUniqueId")
	public void updatePatternPathByUniqueId(@RequestBody ServiceDefinitionDTO serviceDefinitionDTO) throws Exception {
		if(serviceDefinitionDTO != null && serviceDefinitionDTO.getPatternPath()!= null) {
			serviceDefinitionService.updatePatternPathByUniqueId(
					serviceDefinitionDTO.getPrefixPath(),
					serviceDefinitionDTO.getUniqueId(),
					serviceDefinitionDTO.getPatternPath());			
		}
	}
	
	/**
	 * <B>方法名称：</B>updateEnableByUniqueId<BR>
	 * <B>概要说明：</B>根据uniqueId更新服务定义信息<BR>
	 * @author JiFeng
	 * @since 2021年11月29日 上午11:11:25
	 * @param serviceDefinitionDTO
	 * @throws Exception 
	 */
	@RequestMapping("/serviceDefinition/updateEnableByUniqueId")
	public void updateEnableByUniqueId(@RequestBody ServiceDefinitionDTO serviceDefinitionDTO) throws Exception {
		if(serviceDefinitionDTO != null) {
			serviceDefinitionService.updateEnableByUniqueId(
					serviceDefinitionDTO.getPrefixPath(),
					serviceDefinitionDTO.getUniqueId(),
					serviceDefinitionDTO.isEnable());			
		}
	}
	
}
