package com.edan.rapid.console.VO;

import lombok.Data;

import java.util.List;

@Data
public class ServiceMessageVO {

    private List<ServiceDefinitionVO> serviceDefinitionList;

    // 规则总数
    private int ruleQuantity;

    // 实例总数
    private int serviceDefinitionQuantity;

    // 节点总数
    private int serviceInstanceQuantity;

    // 接口总数
    private int serviceInterfaceQuantity;

}
