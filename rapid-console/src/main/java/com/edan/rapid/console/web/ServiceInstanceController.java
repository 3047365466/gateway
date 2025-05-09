package com.edan.rapid.console.web;

import com.edan.rapid.console.VO.ServiceInstanceVO;
import com.edan.rapid.console.dto.ServiceInstanceDTO;
import com.edan.rapid.console.service.ServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * <B>主类名称：</B>ServiceInstanceController<BR>
 * <B>概要说明：</B>服务实例控制层<BR>
 * @author JiFeng
 * @since 2021年11月29日 上午10:41:31
 */
@RestController
public class ServiceInstanceController {

	@Autowired
	private ServiceInstanceService serviceInstanceService;

	/**
	 * <B>方法名称：</B>getList<BR>
	 * <B>概要说明：</B>获取所有服务实例对应的节点信息<BR>
	 * @author JiFeng
	 * @since 2021年11月29日 下午12:54:17
	 * @param prefixPath
	 * @return List<ServiceInstance>
	 * @throws Exception
	 */
	@RequestMapping("/serviceInstance/all")
	public Map<String, List<ServiceInstanceVO>> getList(@RequestParam("prefixPath") String prefixPath) throws Exception{
		return serviceInstanceService.getServiceInstanceAll(prefixPath);
	}
	
	/**
	 * <B>方法名称：</B>getList<BR>
	 * <B>概要说明：</B>根据服务唯一ID获取服务实例列表<BR>
	 * @author JiFeng
	 * @since 2021年11月29日 下午12:54:17
	 * @param prefixPath
	 * @param uniqueId
	 * @return List<ServiceInstance>
	 * @throws Exception 
	 */
	@RequestMapping("/serviceInstance/getList")
	public List<ServiceInstanceVO> getList(@RequestParam("prefixPath") String prefixPath,
										   @RequestParam("uniqueId")String uniqueId) throws Exception{
		List<ServiceInstanceVO> list = serviceInstanceService.getServiceInstanceList(prefixPath, uniqueId);
		return list;
	}
	
	/**
	 * <B>方法名称：</B>updateEnable<BR>
	 * <B>概要说明：</B>启用禁用某个服务实例<BR>
	 * @author JiFeng
	 * @since 2021年11月29日 下午1:04:22
	 * @param serviceInstanceDTO
	 * @throws Exception
	 */
	@RequestMapping("/serviceInstance/updateEnable")
	public void updateEnable(@RequestBody ServiceInstanceDTO serviceInstanceDTO) throws Exception {
		if(serviceInstanceDTO != null) {
			serviceInstanceService.updateEnable(
					serviceInstanceDTO.getPrefixPath(),
					serviceInstanceDTO.getUniqueId(),
					serviceInstanceDTO.getServiceInstanceId(),
					serviceInstanceDTO.isEnable());
		}
	}
	
	/**
	 * <B>方法名称：</B>updateTags<BR>
	 * <B>概要说明：</B>对某个服务实例进行打标签<BR>
	 * @author JiFeng
	 * @since 2021年11月29日 下午1:14:56
	 * @param serviceInstanceDTO
	 * @throws Exception
	 */
	@RequestMapping("/serviceInstance/updateTags")
	public void updateTags(@RequestBody ServiceInstanceDTO serviceInstanceDTO) throws Exception {
		if(serviceInstanceDTO != null) {
			serviceInstanceService.updateTags(
					serviceInstanceDTO.getPrefixPath(),
					serviceInstanceDTO.getUniqueId(),
					serviceInstanceDTO.getServiceInstanceId(),
					serviceInstanceDTO.getTags());
		}
	}
	
	/**
	 * <B>方法名称：</B>updateWeight<BR>
	 * <B>概要说明：</B>更新某个服务实例的权重<BR>
	 * @author JiFeng
	 * @since 2021年11月29日 下午1:15:21
	 * @param serviceInstanceDTO
	 * @throws Exception
	 */
	@RequestMapping("/serviceInstance/updateWeight")
	public void updateWeight(@RequestBody ServiceInstanceDTO serviceInstanceDTO) throws Exception {
		if(serviceInstanceDTO != null) {
			serviceInstanceService.updateWeight(
					serviceInstanceDTO.getPrefixPath(),
					serviceInstanceDTO.getUniqueId(),
					serviceInstanceDTO.getServiceInstanceId(),
					serviceInstanceDTO.getWeight());
		}
	}

}
