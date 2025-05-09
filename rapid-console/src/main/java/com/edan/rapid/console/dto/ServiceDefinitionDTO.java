package com.edan.rapid.console.dto;

public class ServiceDefinitionDTO {
	
	/**
	 * 	前缀
	 */
	private String prefixPath;	// namespace = edanRapid-dev
	/**
	 * 	服务唯一ID
	 */
	private String uniqueId;	//	serviceId:version

    /**
     * 	访问真实ANT表达式匹配
     */
    private String patternPath;
    /**
     *	启用禁用服务
     */
    private boolean enable = true;
    
	public ServiceDefinitionDTO() {
		super();
	}
    
	public ServiceDefinitionDTO(String prefixPath, String uniqueId, String patternPath, boolean enable) {
		super();
		this.prefixPath = prefixPath;
		this.uniqueId = uniqueId;
		this.patternPath = patternPath;
		this.enable = enable;
	}

	public String getPrefixPath() {
		return prefixPath;
	}

	public void setPrefixPath(String prefixPath) {
		this.prefixPath = prefixPath;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getPatternPath() {
		return patternPath;
	}

	public void setPatternPath(String patternPath) {
		this.patternPath = patternPath;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
    
}
