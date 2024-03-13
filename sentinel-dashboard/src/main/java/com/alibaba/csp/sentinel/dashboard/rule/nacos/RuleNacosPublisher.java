package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.config.NacosProperties;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.nacos.api.config.ConfigService;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class RuleNacosPublisher<E extends RuleEntity> implements DynamicRulePublisher<List<E>> {

    private final Function<String, String> appNameToAppId;

    private final ConfigService configService;

    private final Converter<List<E>, String> converter;

    private final NacosProperties nacosProperties;

    public RuleNacosPublisher(Function<String, String> appNameToAppId,
                              ConfigService configService,
                              Converter<List<E>, String> converter,
                              NacosProperties nacosProperties) {
        this.appNameToAppId = appNameToAppId;
        this.configService = configService;
        this.converter = converter;
        this.nacosProperties = nacosProperties;
    }
    @Override
    public void publish(String app, List<E> rules) throws Exception {
        Objects.requireNonNull(app);
        if (rules == null) {
            return;
        }
        String appId = appNameToAppId.apply(app);
        configService.publishConfig(appId + nacosProperties.getFlowKey(),
                nacosProperties.getGroup(), converter.convert(rules));
    }

}
