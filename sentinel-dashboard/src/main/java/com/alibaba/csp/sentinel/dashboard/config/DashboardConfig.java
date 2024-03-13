package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.AppManagement;
import com.alibaba.csp.sentinel.dashboard.discovery.EurekaDiscovery;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineDiscovery;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.discovery.NacosDiscovery;
import com.alibaba.csp.sentinel.dashboard.discovery.SimpleMachineDiscovery;
import com.alibaba.csp.sentinel.dashboard.repository.gateway.InMemApiDefinitionStore;
import com.alibaba.csp.sentinel.dashboard.repository.gateway.InMemGatewayFlowRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.metric.InMemoryMetricsRepository;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemAuthorityRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemDegradeRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemFlowRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemParamFlowRuleStore;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemSystemRuleStore;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.RuleApiProvider;
import com.alibaba.csp.sentinel.dashboard.rule.RuleApiPublisher;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.RuleApolloProvider;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.RuleApolloPublisher;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.RuleNacosProvider;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.RuleNacosPublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.LeaseInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.config.AggregatedConfiguration;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DeploymentContext;
import com.netflix.config.DynamicProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicURLConfiguration;
import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Dashboard配置
 *
 * @author iMinusMinus
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(value = {DashboardProperties.class, ApolloProperties.class, NacosProperties.class})
public class DashboardConfig implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(DashboardConfig.class);

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        DashboardConfig.applicationContext = applicationContext;
    }

    public static int getHideAppNoMachineMillis() {
        if (applicationContext == null) {
            log.warn("com.alibaba.csp.sentinel.dashboard.config.DashboardConfiguration has not been aware");
            return DashboardProperties.AppConfig.MIN_HIDE_APP_NO_MACHINE_MS;
        }
        return applicationContext.getBean(DashboardProperties.class).getApp().getHideAppNoMachineMillis();
    }

    public static int getRemoveAppNoMachineMillis() {
        if (applicationContext == null) {
            log.warn("com.alibaba.csp.sentinel.dashboard.config.DashboardConfiguration has not been aware");
            return DashboardProperties.MIN_REMOVE_APP_NO_MACHINE_MS;
        }
        return applicationContext.getBean(DashboardProperties.class).getRemoveAppNoMachineMillis();
    }

    public static int getAutoRemoveMachineMillis() {
        if (applicationContext == null) {
            log.warn("com.alibaba.csp.sentinel.dashboard.config.DashboardConfiguration has not been aware");
            return DashboardProperties.MIN_AUTO_REMOVE_MACHINE_MILLIS;
        }
        return applicationContext.getBean(DashboardProperties.class).getAutoRemoveMachineMillis();
    }

    public static int getUnhealthyMachineMillis() {
        if (applicationContext == null) {
            log.warn("com.alibaba.csp.sentinel.dashboard.config.DashboardConfiguration has not been aware");
            return DashboardProperties.DEFAULT_UNHEALTHY_MACHINE_MS;
        }
        return applicationContext.getBean(DashboardProperties.class).getUnhealthyMachineMillis();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "sentinel.dashboard.rule", name = "repository", havingValue = "mem", matchIfMissing = true)
    protected static class InMemRepositoryConfiguration {
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


        @Bean
        public InMemoryMetricsRepository metricsRepository() {
            return new InMemoryMetricsRepository();
        }
    }

    // sentinel.dashboard.rule.repository=jdbc

    @Bean
    @ConditionalOnMissingBean(ignored = {AppManagement.class})
    public MachineDiscovery machineDiscovery() {
        return new SimpleMachineDiscovery();
    }

    @Configuration
    @ConditionalOnProperty(prefix = "sentinel.dashboard.rule", name = "discovery", havingValue = "eureka")
    protected static class EurekaDiscoveryConfiguration {

        private static final AtomicBoolean initialized = new AtomicBoolean(false);

        private DynamicURLConfiguration defaultURLConfig;

        @Autowired
        private ConfigurableEnvironment env;

        @PostConstruct
        public void init() {
            if (initialized.compareAndSet(false, true)) {
                String appName = this.env.getProperty("spring.application.name");
                if (appName == null) {
                    appName = "application";
                    log.warn("No spring.application.name found, defaulting to 'application'");
                }
                System.setProperty(DeploymentContext.ContextKey.appId.getKey(), appName);
                ConcurrentCompositeConfiguration config = new ConcurrentCompositeConfiguration();
                config.addConfiguration(new AbstractConfiguration() {
                    @Override
                    protected void addPropertyDirect(String s, Object o) {
                    }

                    @Override
                    public boolean isEmpty() {
                        return !getKeys().hasNext();
                    }

                    @Override
                    public boolean containsKey(String key) {
                        return env.containsProperty(key);
                    }

                    @Override
                    public Object getProperty(String key) {
                        return env.getProperty(key);
                    }

                    @Override
                    public Iterator<String> getKeys() {
                        List<String> result = new ArrayList<>();
                        for (Map.Entry<String, PropertySource<?>> entry : getPropertySources().entrySet()) {
                            PropertySource<?> source = entry.getValue();
                            if (source instanceof EnumerablePropertySource) {
                                EnumerablePropertySource<?> enumerable = (EnumerablePropertySource<?>) source;
                                Collections.addAll(result, enumerable.getPropertyNames());
                            }
                        }
                        return result.iterator();
                    }

                    private Map<String, PropertySource<?>> getPropertySources() {
                        Map<String, PropertySource<?>> map = new LinkedHashMap<>();
                        MutablePropertySources sources = (env != null ? env
                                .getPropertySources() : new StandardEnvironment().getPropertySources());
                        for (PropertySource<?> source : sources) {
                            extract("", map, source);
                        }
                        return map;
                    }

                    private void extract(String root, Map<String, PropertySource<?>> map,
                                         PropertySource<?> source) {
                        if (source instanceof CompositePropertySource) {
                            for (PropertySource<?> nest : ((CompositePropertySource) source)
                                    .getPropertySources()) {
                                extract(source.getName() + ":", map, nest);
                            }
                        }
                        else {
                            map.put(root + source.getName(), source);
                        }
                    }
                });
                defaultURLConfig = new DynamicURLConfiguration();
                try {
                    config.addConfiguration(defaultURLConfig, ConfigurationManager.URL_CONFIG_NAME);
                }
                catch (Throwable ex) {
                    log.error("Cannot create config from " + defaultURLConfig, ex);
                }
                if (ConfigurationManager.isConfigurationInstalled()) {
                    AbstractConfiguration installedConfiguration = ConfigurationManager
                            .getConfigInstance();
                    if (installedConfiguration instanceof ConcurrentCompositeConfiguration) {
                        ConcurrentCompositeConfiguration configInstance = (ConcurrentCompositeConfiguration) installedConfiguration;
                        configInstance.addConfiguration(config);
                    }
                    else {
                        installedConfiguration.append(config);
                        if (!(installedConfiguration instanceof AggregatedConfiguration)) {
                            log.warn(
                                    "Appending a configuration to an existing non-aggregated installed configuration will have no effect");
                        }
                    }
                }
                else {
                    ConfigurationManager.install(config);
                }
            }
        }

        @PreDestroy
        public void close() {
            if (defaultURLConfig != null) {
                defaultURLConfig.stopLoading();
            }
            setStatic(ConfigurationManager.class, "instance", null);
            setStatic(ConfigurationManager.class, "customConfigurationInstalled", false);
            setStatic(DynamicPropertyFactory.class, "config", null);
            setStatic(DynamicPropertyFactory.class, "initializedWithDefaultConfig", false);
            setStatic(DynamicProperty.class, "dynamicPropertySupportImpl", null);
            initialized.compareAndSet(true, false);
        }

        private static void setStatic(Class<?> type, String name, Object value) {
            // Hack a private static field
            Field field = ReflectionUtils.findField(type, name);
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, null, value);
        }

        @Bean
        @ConditionalOnMissingBean
        public EurekaInstanceConfig eurekaInstanceConfig() {
            return new MyDataCenterInstanceConfig("eureka.instance"); //keep same with spring-cloud-starter-netflix-eureka-client
        }

        @Bean
        @ConditionalOnMissingBean
        public EurekaClientConfig eurekaClientConfig() {
            return new DefaultEurekaClientConfig("eureka.client"); //keep same with spring-cloud-starter-netflix-eureka-client
        }

        @Bean
        @ConditionalOnMissingBean
        public ApplicationInfoManager eurekaApplicationInfoManager(EurekaInstanceConfig config) {
            InstanceInfo.Builder builder = InstanceInfo.Builder.newBuilder()
                    .setNamespace(config.getNamespace())
                    .setAppName(config.getAppname())
                    .setInstanceId(config.getInstanceId())
                    .setAppGroupName(config.getAppGroupName())
                    .setDataCenterInfo(config.getDataCenterInfo())
                    .setIPAddr(config.getIpAddress())
                    .setHostName(config.getHostName(false))
                    .setPort(config.getNonSecurePort())
                    .enablePort(InstanceInfo.PortType.UNSECURE, config.isNonSecurePortEnabled())
                    .setSecurePort(config.getSecurePort())
                    .enablePort(InstanceInfo.PortType.SECURE, config.getSecurePortEnabled())
                    .setVIPAddress(config.getVirtualHostName())
                    .setSecureVIPAddress(config.getSecureVirtualHostName())
                    .setHomePageUrl(config.getHomePageUrlPath(), config.getHomePageUrl())
                    .setStatusPageUrl(config.getStatusPageUrlPath(), config.getStatusPageUrl())
                    .setHealthCheckUrls(config.getHealthCheckUrlPath(), config.getHealthCheckUrl(), config.getSecureHealthCheckUrl())
                    .setASGName(config.getASGName());
            if (!config.isInstanceEnabledOnit()) {
                InstanceInfo.InstanceStatus initialStatus = InstanceInfo.InstanceStatus.STARTING;
                if (log.isInfoEnabled()) {
                    log.info("Setting initial instance status as: " + initialStatus);
                }

                builder.setStatus(initialStatus);
            } else if (log.isInfoEnabled()) {
                log.info("Setting initial instance status as: " + InstanceInfo.InstanceStatus.UP + ". This may be too early for the instance to advertise itself as available. You would instead want to control this via a healthcheck handler.");
            }
            for (Map.Entry<String, String> entry : config.getMetadataMap().entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
            LeaseInfo leaseInfo = LeaseInfo.Builder.newBuilder()
                    .setRenewalIntervalInSecs(config.getLeaseRenewalIntervalInSeconds())
                    .setDurationInSecs(config.getLeaseExpirationDurationInSeconds())
                    .build();
            builder.setLeaseInfo(leaseInfo);
            InstanceInfo instanceInfo = builder.build();
            return new ApplicationInfoManager(config, instanceInfo);
        }

        @Bean(destroyMethod = "shutdown")
        @ConditionalOnMissingBean
        public EurekaClient eurekaClient(ApplicationInfoManager manager, EurekaClientConfig config,
                                         ObjectProvider<AbstractDiscoveryClientOptionalArgs<?>> optionalArgs) {
            return new DiscoveryClient(manager, config, optionalArgs.getIfAvailable());
        }

        @Bean
        public MachineDiscovery machineDiscovery(EurekaClient eurekaClient) {
            return new EurekaDiscovery(eurekaClient);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "sentinel.dashboard.rule", name = "discovery", havingValue = "nacos")
    protected static class NacosDiscoveryConfiguration {

        @Bean(destroyMethod = "shutDown")
        @ConditionalOnMissingBean
        public NamingService namingService(NacosProperties nacosProperties) throws Exception {
            return NamingFactory.createNamingService(DashboardConfig.assemble(nacosProperties, true));
        }

        @Bean
        public MachineDiscovery machineDiscovery(NamingService namingService) {
            return new NacosDiscovery(namingService);
        }
    }

    @Bean
    public AppManagement appManagement(MachineDiscovery machineDiscovery) {
        return new AppManagement(machineDiscovery);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRuleProvider<List<AuthorityRuleEntity>> authorityRuleDefaultProvider(SentinelApiClient sentinelApiClient,
                                                                                  AppManagement appManagement) {
        Function<MachineInfo, List<AuthorityRuleEntity>> ruleFetcher =
                (x) -> sentinelApiClient.fetchAuthorityRulesOfMachine(x.getApp(), x.getIp(), x.getPort());
        return new RuleApiProvider(appManagement, ruleFetcher);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRuleProvider<List<DegradeRuleEntity>> degradeRuleDefaultProvider(SentinelApiClient sentinelApiClient,
                                                                                   AppManagement appManagement) {
        Function<MachineInfo, List<DegradeRuleEntity>> ruleFetcher =
                (x) -> sentinelApiClient.fetchDegradeRuleOfMachine(x.getApp(), x.getIp(), x.getPort());
        return new RuleApiProvider(appManagement, ruleFetcher);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRuleProvider<List<FlowRuleEntity>> flowRuleDefaultProvider(SentinelApiClient sentinelApiClient,
                                                                             AppManagement appManagement) {
        Function<MachineInfo, List<FlowRuleEntity>> ruleFetcher =
                (x) -> sentinelApiClient.fetchFlowRuleOfMachine(x.getApp(), x.getIp(), x.getPort());
        return new RuleApiProvider(appManagement, ruleFetcher);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRuleProvider<List<GatewayFlowRuleEntity>> gatewayFlowRuleDefaultProvider(SentinelApiClient sentinelApiClient,
                                                                                    AppManagement appManagement) {
        Function<MachineInfo, List<GatewayFlowRuleEntity>> ruleFetcher =
                (x) -> sentinelApiClient.fetchGatewayFlowRules(x.getApp(), x.getIp(), x.getPort());
        return new RuleApiProvider(appManagement, ruleFetcher);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRuleProvider<List<ParamFlowRuleEntity>> paramFlowRuleDefaultProvider(SentinelApiClient sentinelApiClient,
                                                                                  AppManagement appManagement) {
        Function<MachineInfo, List<ParamFlowRuleEntity>> ruleFetcher =
                (x) -> sentinelApiClient.fetchParamFlowRulesOfMachine(x.getApp(), x.getIp(), x.getPort());
        return new RuleApiProvider(appManagement, ruleFetcher);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRuleProvider<List<SystemRuleEntity>> systemRuleDefaultProvider(SentinelApiClient sentinelApiClient,
                                                                                    AppManagement appManagement) {
        Function<MachineInfo, List<SystemRuleEntity>> ruleFetcher =
                (x) -> sentinelApiClient.fetchSystemRuleOfMachine(x.getApp(), x.getIp(), x.getPort());
        return new RuleApiProvider(appManagement, ruleFetcher);
    }

    @Configuration
    @ConditionalOnProperty(prefix = "sentinel.dashboard.rule", name = "provider", havingValue = "apollo")
    protected static class ApolloRuleProviderConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public Function<String, String> appNameToAppId() {
            return Function.identity();
        }

        @Bean
        public DynamicRuleProvider<List<AuthorityRuleEntity>> authorityRuleDefaultProvider(Function<String, String> appNameToAppId,
                                                                                 ApolloProperties apolloProperties) {
            Converter<String, List<FlowRuleEntity>> converter = s -> JSON.parseArray(s, FlowRuleEntity.class);
            return new RuleApolloProvider(appNameToAppId, converter, apolloProperties);
        }

        @Bean
        public DynamicRuleProvider<List<DegradeRuleEntity>> degradeRuleDefaultProvider(Function<String, String> appNameToAppId,
                                                                                 ApolloProperties apolloProperties) {
            Converter<String, List<DegradeRuleEntity>> converter = s -> JSON.parseArray(s, DegradeRuleEntity.class);
            return new RuleApolloProvider(appNameToAppId, converter, apolloProperties);
        }

        @Bean
        public DynamicRuleProvider<List<FlowRuleEntity>> flowRuleDefaultProvider(Function<String, String> appNameToAppId,
                                                                                 ApolloProperties apolloProperties) {
            Converter<String, List<FlowRuleEntity>> converter = s -> JSON.parseArray(s, FlowRuleEntity.class);
            return new RuleApolloProvider(appNameToAppId, converter, apolloProperties);
        }

        @Bean
        public DynamicRuleProvider<List<GatewayFlowRuleEntity>> gatewayFlowRuleDefaultProvider(Function<String, String> appNameToAppId,
                                                                                           ApolloProperties apolloProperties) {
            Converter<String, List<GatewayFlowRuleEntity>> converter = s -> JSON.parseArray(s, GatewayFlowRuleEntity.class);
            return new RuleApolloProvider(appNameToAppId, converter, apolloProperties);
        }

        @Bean
        public DynamicRuleProvider<List<ParamFlowRuleEntity>> paramFlowRuleDefaultProvider(Function<String, String> appNameToAppId,
                                                                                       ApolloProperties apolloProperties) {
            Converter<String, List<ParamFlowRuleEntity>> converter = s -> JSON.parseArray(s, ParamFlowRuleEntity.class);
            return new RuleApolloProvider(appNameToAppId, converter, apolloProperties);
        }

        @Bean
        public DynamicRuleProvider<List<SystemRuleEntity>> systemRuleDefaultProvider(Function<String, String> appNameToAppId,
                                                                                 ApolloProperties apolloProperties) {
            Converter<String, List<SystemRuleEntity>> converter = s -> JSON.parseArray(s, SystemRuleEntity.class);
            return new RuleApolloProvider(appNameToAppId, converter, apolloProperties);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "sentinel.dashboard.rule", name = "provider", havingValue = "nacos")
    protected static class NacosRuleProviderConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public Function<String, String> appNameToAppId() {
            return Function.identity();
        }

        @Bean
        public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
            return s -> JSON.parseArray(s, FlowRuleEntity.class);
        }

        @Bean(destroyMethod = "shutDown")
        @ConditionalOnMissingBean
        public ConfigService configService(NacosProperties nacosProperties) throws Exception {
            return ConfigFactory.createConfigService(DashboardConfig.assemble(nacosProperties, false));
        }

        @Bean
        public DynamicRuleProvider<List<AuthorityRuleEntity>> authorityRuleDefaultProvider(Function<String, String> appNameToAppId,
                                                                                           ConfigService configService,
                                                                                           NacosProperties nacosProperties) {
            Converter<String, List<FlowRuleEntity>> converter = s -> JSON.parseArray(s, FlowRuleEntity.class);
            return new RuleNacosProvider(appNameToAppId, configService, converter, nacosProperties);
        }

        @Bean
        public DynamicRuleProvider<List<DegradeRuleEntity>> degradeRuleDefaultProvider(Function<String, String> appNameToAppId,
                                                                                       ConfigService configService,
                                                                                       NacosProperties nacosProperties) {
            Converter<String, List<DegradeRuleEntity>> converter = s -> JSON.parseArray(s, DegradeRuleEntity.class);
            return new RuleNacosProvider(appNameToAppId, configService, converter, nacosProperties);
        }

        @Bean
        public DynamicRuleProvider<List<FlowRuleEntity>> flowRuleDefaultProvider(Function<String, String> appNameToAppId,
                                                                                 ConfigService configService,
                                                                                 NacosProperties nacosProperties) {
            Converter<String, List<FlowRuleEntity>> converter = s -> JSON.parseArray(s, FlowRuleEntity.class);
            return new RuleNacosProvider(appNameToAppId, configService, converter, nacosProperties);
        }

        @Bean
        public DynamicRuleProvider<List<GatewayFlowRuleEntity>> gatewayFlowRuleDefaultProvider(Function<String, String> appNameToAppId,
                                                                                               ConfigService configService,
                                                                                               NacosProperties nacosProperties) {
            Converter<String, List<GatewayFlowRuleEntity>> converter = s -> JSON.parseArray(s, GatewayFlowRuleEntity.class);
            return new RuleNacosProvider(appNameToAppId, configService, converter, nacosProperties);
        }

        @Bean
        public DynamicRuleProvider<List<ParamFlowRuleEntity>> paramFlowRuleDefaultProvider(Function<String, String> appNameToAppId,
                                                                                           ConfigService configService,
                                                                                           NacosProperties nacosProperties) {
            Converter<String, List<ParamFlowRuleEntity>> converter = s -> JSON.parseArray(s, ParamFlowRuleEntity.class);
            return new RuleNacosProvider(appNameToAppId, configService, converter, nacosProperties);
        }

        @Bean
        public DynamicRuleProvider<List<SystemRuleEntity>> systemRuleDefaultProvider(Function<String, String> appNameToAppId,
                                                                                     ConfigService configService,
                                                                                     NacosProperties nacosProperties) {
            Converter<String, List<SystemRuleEntity>> converter = s -> JSON.parseArray(s, SystemRuleEntity.class);
            return new RuleNacosProvider(appNameToAppId, configService, converter, nacosProperties);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRulePublisher<List<AuthorityRuleEntity>> authorityRuleDefaultPublisher(SentinelApiClient sentinelApiClient,
                                                                               AppManagement appManagement) {
        BiFunction<MachineInfo, List<AuthorityRuleEntity>, Boolean> function =
                (machine, rules) -> sentinelApiClient.setAuthorityRuleOfMachine(machine.getApp(), machine.getIp(), machine.getPort(), rules);
        return new RuleApiPublisher(appManagement, function);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRulePublisher<List<DegradeRuleEntity>> degradeRuleDefaultPublisher(SentinelApiClient sentinelApiClient,
                                                                               AppManagement appManagement) {
        BiFunction<MachineInfo, List<DegradeRuleEntity>, Boolean> function =
                (machine, rules) -> sentinelApiClient.setDegradeRuleOfMachine(machine.getApp(), machine.getIp(), machine.getPort(), rules);
        return new RuleApiPublisher(appManagement, function);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRulePublisher<List<FlowRuleEntity>> flowRuleDefaultPublisher(SentinelApiClient sentinelApiClient,
                                                                               AppManagement appManagement) {
        BiFunction<MachineInfo, List<FlowRuleEntity>, Boolean> function =
                (machine, rules) -> sentinelApiClient.setFlowRuleOfMachine(machine.getApp(), machine.getIp(), machine.getPort(), rules);
        return new RuleApiPublisher(appManagement, function);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRulePublisher<List<GatewayFlowRuleEntity>> gatewayFlowRuleDefaultPublisher(SentinelApiClient sentinelApiClient,
                                                                               AppManagement appManagement) {
        BiFunction<MachineInfo, List<GatewayFlowRuleEntity>, Boolean> function =
                (machine, rules) -> sentinelApiClient.modifyGatewayFlowRules(machine.getApp(), machine.getIp(), machine.getPort(), rules);
        return new RuleApiPublisher(appManagement, function);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRulePublisher<List<ParamFlowRuleEntity>> paramFlowRuleDefaultPublisher(SentinelApiClient sentinelApiClient,
                                                                               AppManagement appManagement) {
        BiFunction<MachineInfo, List<ParamFlowRuleEntity>, Boolean> function =
                (machine, rules) -> sentinelApiClient.setParamFlowRuleOfMachine(machine.getApp(), machine.getIp(), machine.getPort(), rules);
        return new RuleApiPublisher(appManagement, function);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRulePublisher<List<SystemRuleEntity>> systemRuleDefaultPublisher(SentinelApiClient sentinelApiClient,
                                                                               AppManagement appManagement) {
        BiFunction<MachineInfo, List<SystemRuleEntity>, Boolean> function =
                (machine, rules) -> sentinelApiClient.setSystemRuleOfMachine(machine.getApp(), machine.getIp(), machine.getPort(), rules);
        return new RuleApiPublisher(appManagement, function);
    }

    @Configuration
    @ConditionalOnProperty(prefix = "sentinel.dashboard.rule", name = "publisher", havingValue = "apollo")
    protected static class ApolloRulePublisherConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public Function<String, String> appNameToAppId() {
            return Function.identity();
        }

        @Bean
        public DynamicRulePublisher<List<AuthorityRuleEntity>> authorityRuleDefaultPublisher(Function<String, String> appNameToAppId,
                                                                                   ApolloProperties apolloProperties) {
            return new RuleApolloPublisher(appNameToAppId, JSON::toJSONString, apolloProperties);
        }

        @Bean
        public DynamicRulePublisher<List<DegradeRuleEntity>> degradeRuleDefaultPublisher(Function<String, String> appNameToAppId,
                                                                                   ApolloProperties apolloProperties) {
            return new RuleApolloPublisher(appNameToAppId, JSON::toJSONString, apolloProperties);
        }

        @Bean
        public DynamicRulePublisher<List<FlowRuleEntity>> flowRuleDefaultPublisher(Function<String, String> appNameToAppId,
                                                                                   ApolloProperties apolloProperties) {
            return new RuleApolloPublisher(appNameToAppId, JSON::toJSONString, apolloProperties);
        }

        @Bean
        public DynamicRulePublisher<List<GatewayFlowRuleEntity>> gatewayFlowRuleDefaultPublisher(Function<String, String> appNameToAppId,
                                                                                             ApolloProperties apolloProperties) {
            return new RuleApolloPublisher(appNameToAppId, JSON::toJSONString, apolloProperties);
        }

        @Bean
        public DynamicRulePublisher<List<ParamFlowRuleEntity>> paramFlowRuleDefaultPublisher(Function<String, String> appNameToAppId,
                                                                                         ApolloProperties apolloProperties) {
            return new RuleApolloPublisher(appNameToAppId, JSON::toJSONString, apolloProperties);
        }

        @Bean
        public DynamicRulePublisher<List<SystemRuleEntity>> systemRuleDefaultPublisher(Function<String, String> appNameToAppId,
                                                                                   ApolloProperties apolloProperties) {
            return new RuleApolloPublisher(appNameToAppId, JSON::toJSONString, apolloProperties);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "sentinel.dashboard.rule", name = "publisher", havingValue = "nacos")
    protected static class NacosRulePublisherConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public Function<String, String> appNameToAppId() {
            return Function.identity();
        }

        @Bean(destroyMethod = "shutDown")
        @ConditionalOnMissingBean
        public ConfigService configService(NacosProperties nacosProperties) throws Exception {
            return ConfigFactory.createConfigService(DashboardConfig.assemble(nacosProperties, false));
        }

        @Bean
        public DynamicRulePublisher<List<AuthorityRuleEntity>> authorityRuleDefaultPublisher(Function<String, String> appNameToAppId,
                                                                                   ConfigService configService,
                                                                                   NacosProperties nacosProperties) {
            return new RuleNacosPublisher(appNameToAppId, configService, JSON::toJSONString, nacosProperties);
        }

        @Bean
        public DynamicRulePublisher<List<DegradeRuleEntity>> degradeRuleDefaultPublisher(Function<String, String> appNameToAppId,
                                                                                   ConfigService configService,
                                                                                   NacosProperties nacosProperties) {
            return new RuleNacosPublisher(appNameToAppId, configService, JSON::toJSONString, nacosProperties);
        }

        @Bean
        public DynamicRulePublisher<List<FlowRuleEntity>> flowRuleDefaultPublisher(Function<String, String> appNameToAppId,
                                                                                   ConfigService configService,
                                                                                   NacosProperties nacosProperties) {
            return new RuleNacosPublisher(appNameToAppId, configService, JSON::toJSONString, nacosProperties);
        }

        @Bean
        public DynamicRulePublisher<List<GatewayFlowRuleEntity>> gatewayFlowRuleDefaultPublisher(Function<String, String> appNameToAppId,
                                                                                             ConfigService configService,
                                                                                             NacosProperties nacosProperties) {
            return new RuleNacosPublisher(appNameToAppId, configService, JSON::toJSONString, nacosProperties);
        }

        @Bean
        public DynamicRulePublisher<List<ParamFlowRuleEntity>> paramFlowRuleDefaultPublisher(Function<String, String> appNameToAppId,
                                                                                         ConfigService configService,
                                                                                         NacosProperties nacosProperties) {
            return new RuleNacosPublisher(appNameToAppId, configService, JSON::toJSONString, nacosProperties);
        }

        @Bean
        public DynamicRulePublisher<List<SystemRuleEntity>> systemRuleDefaultPublisher(Function<String, String> appNameToAppId,
                                                                                   ConfigService configService,
                                                                                   NacosProperties nacosProperties) {
            return new RuleNacosPublisher(appNameToAppId, configService, JSON::toJSONString, nacosProperties);
        }
    }

    private static Properties assemble(com.alibaba.csp.sentinel.dashboard.config.NacosProperties nacosProperties, boolean naming) {
        Properties properties = new Properties();
        properties.put(com.alibaba.nacos.api.annotation.NacosProperties.PREFIX + com.alibaba.nacos.api.annotation.NacosProperties.SERVER_ADDR, nacosProperties.getServerAddr());
        properties.put(com.alibaba.nacos.api.annotation.NacosProperties.PREFIX + com.alibaba.nacos.api.annotation.NacosProperties.USERNAME, nacosProperties.getUsername());
        properties.put(com.alibaba.nacos.api.annotation.NacosProperties.PREFIX + com.alibaba.nacos.api.annotation.NacosProperties.PASSWORD, nacosProperties.getPassword());
        String namespace = nacosProperties.getNamespace();
        if (naming && (namespace == null || namespace.length() == 0)) {
            namespace = "public";
        }
        properties.put(com.alibaba.nacos.api.annotation.NacosProperties.PREFIX + com.alibaba.nacos.api.annotation.NacosProperties.NAMESPACE, namespace);
        return properties;
    }
}
