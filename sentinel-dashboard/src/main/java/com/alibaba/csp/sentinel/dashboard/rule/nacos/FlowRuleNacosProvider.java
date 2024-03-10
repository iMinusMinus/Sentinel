package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.config.NacosProperties;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FlowRuleNacosProvider implements DynamicRuleProvider<List<FlowRuleEntity>> {

    private final Function<String, String> appNameToAppId;

    private final ConfigService configService;

    private final Converter<String, List<FlowRuleEntity>> converter;

    private final NacosProperties nacosProperties;

    public FlowRuleNacosProvider(Function<String, String> appNameToAppId,
                                 ConfigService configService,
                                 Converter<String, List<FlowRuleEntity>> converter,
                                 NacosProperties nacosProperties) {
        this.appNameToAppId = appNameToAppId;
        this.configService = configService;
        this.converter = converter;
        this.nacosProperties = nacosProperties;
    }

    @Override
    public List<FlowRuleEntity> getRules(String appName) throws Exception {
        String appId = appNameToAppId.apply(appName);
        String rules = configService.getConfig(appId + nacosProperties.getFlowKey(),
                nacosProperties.getGroup(), nacosProperties.getReadTimeout());
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return converter.convert(rules);
    }

}
