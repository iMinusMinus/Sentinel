package com.alibaba.csp.sentinel.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nacos 数据模型 Key 由三元组唯一确定：Namespace、Group、DataId
 */
@ConfigurationProperties(prefix = "nacos")
public class NacosProperties {

    private String serverAddr;

    private String username;

    private String password;

    private int readTimeout = 3000;

    private String accessToken;

    /**
     * 不同的命名空间下，可以存在相同的 Group 或 Data ID 的配置。
     * Namespace 的常用场景之一是不同环境的配置的区分隔离
     */
    private String namespace;

    /**
     * Nacos 中的一组配置集，是组织配置的维度之一。
     */
    private String group = "DEFAULT_GROUP";

    private String authorityKey = "authority";

    private String degradeKey = "degrade";

    private String gatewayApiGroupKey = "gw-api-group";

    private String gatewayFlowKey = "gw-flow";

    private String flowKey = "flow";

    private String paramFlowKey = "param-flow";

    private String systemKey = "system";

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getAuthorityKey() {
        return authorityKey;
    }

    public void setAuthorityKey(String authorityKey) {
        this.authorityKey = authorityKey;
    }

    public String getDegradeKey() {
        return degradeKey;
    }

    public void setDegradeKey(String degradeKey) {
        this.degradeKey = degradeKey;
    }

    public String getGatewayApiGroupKey() {
        return gatewayApiGroupKey;
    }

    public void setGatewayApiGroupKey(String gatewayApiGroupKey) {
        this.gatewayApiGroupKey = gatewayApiGroupKey;
    }

    public String getGatewayFlowKey() {
        return gatewayFlowKey;
    }

    public void setGatewayFlowKey(String gatewayFlowKey) {
        this.gatewayFlowKey = gatewayFlowKey;
    }

    public String getFlowKey() {
        return flowKey;
    }

    public void setFlowKey(String flowKey) {
        this.flowKey = flowKey;
    }

    public String getParamFlowKey() {
        return paramFlowKey;
    }

    public void setParamFlowKey(String paramFlowKey) {
        this.paramFlowKey = paramFlowKey;
    }

    public String getSystemKey() {
        return systemKey;
    }

    public void setSystemKey(String systemKey) {
        this.systemKey = systemKey;
    }
}
