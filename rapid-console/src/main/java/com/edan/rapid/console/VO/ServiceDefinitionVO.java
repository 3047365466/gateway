package com.edan.rapid.console.VO;

import com.edan.rapid.common.config.ServiceDefinition;
import lombok.Data;

import java.util.List;

@Data
public class ServiceDefinitionVO extends ServiceDefinition {

    private static final long serialVersionUID = -847706887465809170L;

    private List<ServiceInstanceVO> serviceInstanceList;

    // ServiceDefinition json详情
//    private String json;
}
