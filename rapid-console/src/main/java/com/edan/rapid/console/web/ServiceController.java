package com.edan.rapid.console.web;

import com.edan.rapid.common.config.Rule;
import com.edan.rapid.console.VO.ServiceDefinitionVO;
import com.edan.rapid.console.VO.ServiceInstanceVO;
import com.edan.rapid.console.VO.ServiceMessageVO;
import com.edan.rapid.console.service.RuleService;
import com.edan.rapid.console.service.ServiceDefinitionService;
import com.edan.rapid.console.service.ServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <B>主类名称：</B>RuleController<BR>
 * <B>概要说明：</B>综合控制层<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午10:55:41
 */
@RestController
public class ServiceController {

    @Autowired
    private RuleService ruleService;

    @Autowired
    private ServiceDefinitionService serviceDefinitionService;

    @Autowired
    private ServiceInstanceService serviceInstanceService;

    @RequestMapping("message/all")
    public ServiceMessageVO getList(@RequestParam("prefixPath") String prefixPath) throws Exception{
        ServiceMessageVO vo = new ServiceMessageVO();
        int ruleQuantity = 0;
        int serviceDefinitionQuantity = 0;
        int serviceInstanceQuantity = 0;
        int serviceInterfaceQuantity = 0;
        List<Rule> ruleList = ruleService.getRuleList(prefixPath);
        List<ServiceDefinitionVO> serviceDefinitionList = serviceDefinitionService.getServiceDefinitionList(prefixPath);
//        List<ServiceInstance> serviceInstanceList = new ArrayList<>(serviceDefinitionList.size() * 2);
        for (ServiceDefinitionVO serviceDefinitionVO : serviceDefinitionList) {
            List<ServiceInstanceVO> serviceInstanceList = serviceInstanceService.getServiceInstanceList(prefixPath,
                    serviceDefinitionVO.getUniqueId());
            serviceDefinitionVO.setServiceInstanceList(serviceInstanceList);
            serviceInstanceQuantity += serviceInstanceList.size();
            serviceInterfaceQuantity += serviceDefinitionVO.getInvokerMap().size();
        }

        ruleQuantity = ruleList.size();
        serviceDefinitionQuantity = serviceDefinitionList.size();

        vo.setServiceDefinitionList(serviceDefinitionList);
        vo.setRuleQuantity(ruleQuantity);
        vo.setServiceDefinitionQuantity(serviceDefinitionQuantity);
        vo.setServiceInstanceQuantity(serviceInstanceQuantity);
        vo.setServiceInterfaceQuantity(serviceInterfaceQuantity);
        return vo;
    }


}
