package com.edan.rapid.console.dto;

import com.edan.rapid.common.config.Rule;
import com.edan.rapid.common.config.Rule.FilterConfig;

import java.util.HashSet;
import java.util.Set;

public class RuleDTO {
	
	//	前缀
	private String prefixPath;

	//	规则ID
    private String id;
    
    //	规则名称
    private String name;
    
    //	服务唯一Id
    private String uniqueId;
    
    //  route对应的协议
    private String protocol;
    
    //	规则排序
    private Integer order;
    
    //	规则集合
    private Set<Rule.FilterConfig> filterConfigs = new HashSet<>();

	public RuleDTO() {
		super();
	}

	public RuleDTO(String prefixPath, String id, String name, String uniqueId, String protocol, Integer order,
			Set<FilterConfig> filterConfigs) {
		super();
		this.prefixPath = prefixPath;
		this.id = id;
		this.name = name;
		this.uniqueId = uniqueId;
		this.protocol = protocol;
		this.order = order;
		this.filterConfigs = filterConfigs;
	}

	public String getPrefixPath() {
		return prefixPath;
	}

	public void setPrefixPath(String prefixPath) {
		this.prefixPath = prefixPath;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public Set<Rule.FilterConfig> getFilterConfigs() {
		return filterConfigs;
	}

	public void setFilterConfigs(Set<Rule.FilterConfig> filterConfigs) {
		this.filterConfigs = filterConfigs;
	}
    
    
    
}
