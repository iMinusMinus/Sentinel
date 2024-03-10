package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.config.ApolloProperties;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * apollo 动态获取规则
 *
 * @author iMinusMinus
 */
public class FlowRuleApolloProvider implements DynamicRuleProvider<List<FlowRuleEntity>> {

    private final Function<String, String> appNameToAppId;

    private final ApolloOpenApiClient apolloOpenApiClient;

    private final Converter<String, List<FlowRuleEntity>> converter;

    private final ApolloProperties apolloProperties;

    public FlowRuleApolloProvider(Function<String, String> appNameToAppId,
                                  Converter<String, List<FlowRuleEntity>> converter,
                                  ApolloProperties apolloProperties) {
        this.appNameToAppId = appNameToAppId;
        this.converter = converter;
        this.apolloProperties = apolloProperties;
        this.apolloOpenApiClient = ApolloOpenApiClient.newBuilder()
                .withPortalUrl(apolloProperties.getPortalUrl())
                .withToken(apolloProperties.getToken())
                .withConnectTimeout(apolloProperties.getConnectTimeout())
                .withReadTimeout(apolloProperties.getReadTimeout())
                .build();
    }

    @Override
    public List<FlowRuleEntity> getRules(String appName) throws Exception {
        String appId = appNameToAppId.apply(appName);
        String flowDataId = ApolloConfigUtil.formatKey(apolloProperties.isSameAppId(), appId, apolloProperties.getFlowKey());
        OpenNamespaceDTO openNamespaceDTO = apolloOpenApiClient.getNamespace(appId, apolloProperties.getEnv(),
                apolloProperties.getCluster(), apolloProperties.getNamespace());
        String rules = openNamespaceDTO
                .getItems()
                .stream()
                .filter(p -> p.getKey().equals(flowDataId))
                .map(OpenItemDTO::getValue)
                .findFirst()
                .orElse("");

        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return converter.convert(rules);
    }
}
