package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.config.ApolloProperties;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;

import java.util.List;
import java.util.function.Function;

/**
 * publish to apollo
 *
 * @author iMinusMinus
 */
public class FlowRuleApolloPublisher implements DynamicRulePublisher<List<FlowRuleEntity>> {

    private final Function<String, String> appNameToAppId;

    private final ApolloOpenApiClient apolloOpenApiClient;

    private final Converter<List<FlowRuleEntity>, String> converter;

    private final ApolloProperties apolloProperties;

    public FlowRuleApolloPublisher(Function<String, String> appNameToAppId,
                                   Converter<List<FlowRuleEntity>, String> converter,
                                   ApolloProperties apolloProperties) {
        this.appNameToAppId = appNameToAppId;
        this.converter = converter;
        this.apolloProperties = apolloProperties;
        this.apolloOpenApiClient =  ApolloOpenApiClient.newBuilder()
                .withPortalUrl(apolloProperties.getPortalUrl())
                .withToken(apolloProperties.getToken())
                .withConnectTimeout(apolloProperties.getConnectTimeout())
                .withReadTimeout(apolloProperties.getReadTimeout())
                .build();
    }

    @Override
    public void publish(String app, List<FlowRuleEntity> rules) throws Exception {
        if (rules == null) {
            return;
        }

        // Increase the configuration
        String appId = appNameToAppId.apply(app);
        String flowDataId = ApolloConfigUtil.formatKey(apolloProperties.isSameAppId(), appId, apolloProperties.getFlowKey());
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(flowDataId);
        openItemDTO.setValue(converter.convert(rules));
        openItemDTO.setComment("Program auto-join");
        openItemDTO.setDataChangeCreatedBy(apolloProperties.getUsername()); // TODO fetch user from authentication
        apolloOpenApiClient.createOrUpdateItem(appId, apolloProperties.getEnv(), apolloProperties.getCluster(), apolloProperties.getNamespace(), openItemDTO);

        // Release configuration
        NamespaceReleaseDTO namespaceReleaseDTO = new NamespaceReleaseDTO();
        namespaceReleaseDTO.setEmergencyPublish(true);
        namespaceReleaseDTO.setReleaseComment("Modify or add configurations");
        namespaceReleaseDTO.setReleasedBy(apolloProperties.getUsername()); // TODO fetch user from authentication
        namespaceReleaseDTO.setReleaseTitle("Modify or add configurations");
        apolloOpenApiClient.publishNamespace(appId, apolloProperties.getEnv(), apolloProperties.getCluster(), apolloProperties.getNamespace(), namespaceReleaseDTO);
    }
}
