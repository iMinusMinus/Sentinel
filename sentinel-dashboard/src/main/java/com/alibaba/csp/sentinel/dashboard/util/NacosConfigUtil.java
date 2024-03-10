package com.alibaba.csp.sentinel.dashboard.util;


import com.alibaba.nacos.api.annotation.NacosProperties;

import java.util.Properties;

public abstract class NacosConfigUtil {

    public static Properties assemble(com.alibaba.csp.sentinel.dashboard.config.NacosProperties nacosProperties, boolean naming) {
        Properties properties = new Properties();
        properties.put(NacosProperties.PREFIX + NacosProperties.SERVER_ADDR, nacosProperties.getServerAddr());
        properties.put(NacosProperties.PREFIX + NacosProperties.USERNAME, nacosProperties.getUsername());
        properties.put(NacosProperties.PREFIX + NacosProperties.PASSWORD, nacosProperties.getPassword());
        String namespace = nacosProperties.getNamespace();
        if (naming && (namespace == null || namespace.length() == 0)) {
            namespace = "public";
        }
        properties.put(NacosProperties.PREFIX + NacosProperties.NAMESPACE, namespace);
        return properties;
    }
}
