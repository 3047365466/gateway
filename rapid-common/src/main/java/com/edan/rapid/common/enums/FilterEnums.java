package com.edan.rapid.common.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum FilterEnums {


    LOAD_BALANCE_PREFILTER("loadBalancePreFilter", "前置过滤器"),
    LIMIT_PRE_FILTER("limitPreFilter", "前置过滤器"),
    TIMEOUT_PRE_FILTER_ID("timeoutPreFilter", "前置过滤器"),
    STATISTICS_POST_FILTER_ID("statisticsPostFilter", "后置过滤器");
    private String name;
    private String type;

    FilterEnums(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public static Map<String, List<String>> all() {
        HashMap<String, List<String>> map = new HashMap<>();
        for (FilterEnums value : FilterEnums.values()) {
            List<String> list = map.getOrDefault(value.type, new ArrayList<>());
            list.add(value.name);
            map.put(value.type, list);
        }
        return map;
    }
}
