package com.edan.rapid.console.service;

import com.edan.rapid.common.config.Rule;
import com.edan.rapid.common.util.FastJsonConvertUtil;
import com.edan.rapid.common.util.Pair;
import com.edan.rapid.discovery.api.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <B>主类名称：</B>RuleService<BR>
 * <B>概要说明：</B>RuleService<BR>
 * @author JiFeng
 * @since 2021年12月20日 下午10:54:23
 */
@Service
public class RuleService {
	
	@Autowired
	private RegistryService registryService;

	/**
	 * <B>方法名称：</B>getRuleList<BR>
	 * <B>概要说明：</B>获取所有的规则列表<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 下午10:53:49
	 * @param prefixPath
	 * @return
	 * @throws Exception 
	 */
	public List<Rule> getRuleList(String prefixPath) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.RULE_PREFIX;
		List<Pair<String, String>> list = registryService.getListByPrefixKey(path);
		List<Rule> rules = new ArrayList<Rule>();
		for(Pair<String, String> pair : list) {
			String p = pair.getObject1();
			if (p.equals(path)) { 
				continue;
			}
			String json = pair.getObject2();
			Rule rule = FastJsonConvertUtil.convertJSONToObject(json, Rule.class);
			rules.add(rule);
		}
		return rules;
	}

	/**
	 * <B>方法名称：</B>addRule<BR>
	 * <B>概要说明：</B>addRule<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 下午10:54:01
	 * @param prefixPath
	 * @param rule
	 * @throws Exception
	 */
	public void addRule(String prefixPath, Rule rule) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.RULE_PREFIX;	
		String key = path + RegistryService.PATH + rule.getId();
		String value = FastJsonConvertUtil.convertObjectToJSON(rule);
		registryService.registerPersistentNode(key, value);	
	}
	
	/**
	 * <B>方法名称：</B>updateRule<BR>
	 * <B>概要说明：</B>updateRule<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 下午10:54:08
	 * @param prefixPath
	 * @param rule
	 * @throws Exception
	 */
	public void updateRule(String prefixPath, Rule rule) throws Exception {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.RULE_PREFIX;	
		String key = path + RegistryService.PATH + rule.getId();
		String value = FastJsonConvertUtil.convertObjectToJSON(rule);
		registryService.registerPersistentNode(key, value);
	}

	/**
	 * <B>方法名称：</B>deleteRule<BR>
	 * <B>概要说明：</B>deleteRule<BR>
	 * @author JiFeng
	 * @since 2021年12月20日 下午10:54:14
	 * @param prefixPath
	 * @param ruleId
	 */
	public void deleteRule(String prefixPath, String ruleId) {
		String path = RegistryService.PATH 
				+ prefixPath 
				+ RegistryService.RULE_PREFIX;	
		String key = path + RegistryService.PATH + ruleId;
		registryService.deleteByKey(key);
	}
	
}
