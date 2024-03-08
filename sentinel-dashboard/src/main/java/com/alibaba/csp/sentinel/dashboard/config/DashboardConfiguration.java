package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.repository.gateway.InMemApiDefinitionStore;
import com.alibaba.csp.sentinel.dashboard.repository.gateway.InMemGatewayFlowRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemAuthorityRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemDegradeRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemFlowRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemParamFlowRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemSystemRuleStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dashboard配置
 *
 * @author iMinusMinus
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(value = {DashboardProperties.class})
public class DashboardConfiguration {

    @Configuration
    @ConditionalOnProperty(prefix = "sentinel.dashboard.rule", name = "repository", matchIfMissing = true)
    protected static class SimpleInMemRuleStoreConfiguration {

        @Bean
        public InMemApiDefinitionStore apiDefinitionStore() {
            return new InMemApiDefinitionStore();
        }

        @Bean
        public InMemAuthorityRuleStore authorityRuleStore() {
            return new InMemAuthorityRuleStore();
        }

        @Bean
        public InMemDegradeRuleStore degradeRuleStore() {
            return new InMemDegradeRuleStore();
        }

        @Bean
        public InMemFlowRuleStore flowRuleStore() {
            return new InMemFlowRuleStore();
        }

        @Bean
        public InMemGatewayFlowRuleStore gatewayFlowRuleStore() {
            return new InMemGatewayFlowRuleStore();
        }

        @Bean
        public InMemParamFlowRuleStore paramFlowRuleStore() {
            return new InMemParamFlowRuleStore();
        }

        @Bean
        public InMemSystemRuleStore systemRuleStore() {
            return new InMemSystemRuleStore();
        }
    }
}
