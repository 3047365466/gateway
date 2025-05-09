package com.edan.rapid.console.dto;

public class ServiceInstanceDTO {

	/**
	 * 	前缀
	 */
	private String prefixPath;
	/**
	 * 	服务唯一ID
	 */
	private String uniqueId;

	/**
	 * 	服务实例ID = address
	 */
	private String serviceInstanceId;
	
    /**
     *	启用禁用服务实例
     */
    private boolean enable = true;
    
    /**
     * 	路由标签
     */
    private String tags;
    
    /**
     * 	权重
     */
    private Integer weight;
    
	public ServiceInstanceDTO() {
		super();
	}

	public ServiceInstanceDTO(String prefixPath, String uniqueId, String serviceInstanceId, boolean enable, String tags,
			Integer weight) {
		super();
		this.prefixPath = prefixPath;
		this.uniqueId = uniqueId;
		this.serviceInstanceId = serviceInstanceId;
		this.enable = enable;
		this.tags = tags;
		this.weight = weight;
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

	public String getServiceInstanceId() {
		return serviceInstanceId;
	}

	public void setServiceInstanceId(String serviceInstanceId) {
		this.serviceInstanceId = serviceInstanceId;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}
    
}
