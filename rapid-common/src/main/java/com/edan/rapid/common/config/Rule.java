package com.edan.rapid.common.config;

import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @Description 功能描述
 * @Author Edan
 * @Create 2024/8/29 3:35
 */
@Data
public class Rule implements Comparable<Rule>, Serializable {


    private static final long serialVersionUID = 1393804716511354771L;

    private String name;

    private String id;

    // 协议
    private String protocol;
    // 规则优先级
    private Integer order;
    // 匹配的规则过滤器
    private Set<Rule.FilterConfig> filterConfigs = new HashSet<>();


    /**
     * <B>方法名称：</B>addFilterConfig<BR>
     * <B>概要说明：</B>向规则里面添加指定的过滤器<BR>
     * @author JiFeng
     * @since 2021年12月9日 下午2:21:07
     * @param filterConfig
     * @return
     */
    public boolean addFilterConfig(Rule.FilterConfig filterConfig) {
        return filterConfigs.add(filterConfig);
    }

    /**
     * <B>方法名称：</B>getFilterConfig<BR>
     * <B>概要说明：</B>通过一个指定的filterId 获取getFilterConfig<BR>
     * @author JiFeng
     * @since 2021年12月9日 下午2:22:57
     * @param id
     * @return Rule.FilterConfig
     */
    public Rule.FilterConfig getFilterConfig(String id){
        for(Rule.FilterConfig filterConfig : filterConfigs) {
            if(filterConfig.getId().equalsIgnoreCase(id)) {
                return filterConfig;
            }
        }
        return null;
    }

    /**
     * <B>方法名称：</B>hashId<BR>
     * <B>概要说明：</B>根据传入的filterId 判断当前Rule中是否存在<BR>
     * @author JiFeng
     * @since 2021年12月9日 下午2:24:27
     * @param id
     * @return boolean
     */
    public boolean hashId(String id) {
        for(Rule.FilterConfig filterConfig : filterConfigs) {
            if(filterConfig.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Rule o) {
        return this.order.compareTo(o.order);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if((o == null) || getClass() != o.getClass()) return false;
        Rule that = (Rule)o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * <B>主类名称：</B>FilterConfig<BR>
     * <B>概要说明：</B>过滤器的配置类<BR>
     * @author JiFeng
     * @since 2021年12月9日 下午2:10:13
     */
    public static class FilterConfig {

        //	过滤器的唯一ID
        private String id;

        //	过滤器的配置信息描述：json string
        private String config;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if((o == null) || getClass() != o.getClass()) return false;
            FilterConfig that = (FilterConfig)o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

}
