package com.edan.rapid.console.web;

import com.edan.rapid.common.config.Rule;
import com.edan.rapid.common.constants.ProcessorFilterConstants;
import com.edan.rapid.common.constants.RapidProtocol;
import com.edan.rapid.common.enums.FilterEnums;
import com.edan.rapid.common.util.FastJsonConvertUtil;
import com.edan.rapid.console.dto.RuleDTO;
import com.edan.rapid.console.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.edan.rapid.common.constants.ProcessorFilterConstants.DUBBO_ROUTE_FILTER_ID;
import static com.edan.rapid.common.constants.ProcessorFilterConstants.HTTP_ROUTE_FILTER_ID;

/**
 * <B>主类名称：</B>RuleController<BR>
 * <B>概要说明：</B>规则控制层<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午10:55:41
 */
@RestController
public class RuleController {

	@Autowired
	private RuleService ruleService;

	@RequestMapping("rule/getFilterMap")
	public Map<String, List<String>> getFilterMap() {
		return FilterEnums.all();
	}
	
	@RequestMapping("rule/getList")
	public List<Rule> getList(@RequestParam("prefixPath") String prefixPath) throws Exception{
		List<Rule> list = ruleService.getRuleList(prefixPath);
		return list;
	}
	
	@RequestMapping("rule/add")
	public void addRule(@RequestBody RuleDTO ruleDTO) throws Exception {
		if(ruleDTO != null) {
			Rule rule = new Rule();
			rule.setId(ruleDTO.getId());
			rule.setName(ruleDTO.getName());
			rule.setProtocol(ruleDTO.getProtocol());
			rule.setOrder(ruleDTO.getOrder());
			rule.setFilterConfigs(ruleDTO.getFilterConfigs());
			if (RapidProtocol.isHttp(ruleDTO.getProtocol())) {
				Rule.FilterConfig routeFilter = new Rule.FilterConfig();
				routeFilter.setId(HTTP_ROUTE_FILTER_ID);
				Map<String, String> param = new HashMap<>();
				param.put("loggable", "false");
				String configStr = FastJsonConvertUtil.convertObjectToJSON(param);
				routeFilter.setConfig(configStr);
				ruleDTO.getFilterConfigs().add(routeFilter);
			} else if (RapidProtocol.isDubbo(ruleDTO.getProtocol())) {
				Rule.FilterConfig routeFilter = new Rule.FilterConfig();
				routeFilter.setId(DUBBO_ROUTE_FILTER_ID);
				Map<String, String> param = new HashMap<>();
				param.put("loggable", "false");
				String configStr = FastJsonConvertUtil.convertObjectToJSON(param);
				routeFilter.setConfig(configStr);
				ruleDTO.getFilterConfigs().add(routeFilter);
			} else {
				throw new RuntimeException("不支持的协议");
			}

			Rule.FilterConfig errorFilter = new Rule.FilterConfig();
			errorFilter.setId(ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ID);
			Map<String, String> param = new HashMap<>();
			param.put("loggable", "false");
			String configStr4 = FastJsonConvertUtil.convertObjectToJSON(param);
			errorFilter.setConfig(configStr4);
			ruleDTO.getFilterConfigs().add(errorFilter);
			ruleService.addRule(ruleDTO.getPrefixPath(), rule);			
		}
	}
	
	@RequestMapping("rule/update")
	public void updateRule(@RequestBody RuleDTO ruleDTO) throws Exception {
		if(ruleDTO != null) {
			Rule rule = new Rule();
			rule.setId(ruleDTO.getId());
			rule.setName(ruleDTO.getName());
			rule.setProtocol(ruleDTO.getProtocol());
			rule.setOrder(ruleDTO.getOrder());

			if (RapidProtocol.isHttp(ruleDTO.getProtocol())) {
				Rule.FilterConfig routeFilter = new Rule.FilterConfig();
				routeFilter.setId(HTTP_ROUTE_FILTER_ID);
				Map<String, String> param = new HashMap<>();
				param.put("loggable", "false");
				String configStr = FastJsonConvertUtil.convertObjectToJSON(param);
				routeFilter.setConfig(configStr);
				ruleDTO.getFilterConfigs().add(routeFilter);
			} else if (RapidProtocol.isDubbo(ruleDTO.getProtocol())) {
				Rule.FilterConfig routeFilter = new Rule.FilterConfig();
				routeFilter.setId(DUBBO_ROUTE_FILTER_ID);
				Map<String, String> param = new HashMap<>();
				param.put("loggable", "false");
				String configStr = FastJsonConvertUtil.convertObjectToJSON(param);
				routeFilter.setConfig(configStr);
				ruleDTO.getFilterConfigs().add(routeFilter);
			} else {
				throw new RuntimeException("不支持的协议");
			}

			Rule.FilterConfig errorFilter = new Rule.FilterConfig();
			errorFilter.setId(ProcessorFilterConstants.DEFAULT_ERROR_FILTER_ID);
			Map<String, String> param = new HashMap<>();
			param.put("loggable", "false");
			String configStr4 = FastJsonConvertUtil.convertObjectToJSON(param);
			errorFilter.setConfig(configStr4);
			ruleDTO.getFilterConfigs().add(errorFilter);
			rule.setFilterConfigs(ruleDTO.getFilterConfigs());
			ruleService.updateRule(ruleDTO.getPrefixPath(), rule);			
		}
	}
	
	@RequestMapping("rule/delete")
	public void deleteRule(@RequestBody RuleDTO ruleDTO) {
		if(ruleDTO != null) {
			ruleService.deleteRule(ruleDTO.getPrefixPath(), ruleDTO.getId());	
		}
	}
	
}
