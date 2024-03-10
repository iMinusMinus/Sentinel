package com.alibaba.csp.sentinel.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sentinel.apollo")
public class ApolloProperties {

    /**
     * apollo portal url
     */
    private String portalUrl;

    /**
     * apollo token
     */
    private String token;

    private int connectTimeout = 1000;

    private int readTimeout = 5000;

    /**
     * apollo env
     */
    private String env;

    /**
     * apollo cluster
     */
    private String cluster = "default";

    /**
     * apollo namespace
     */
    private String namespace = "application";

    /**
     * apollo username, must exists!
     */
    private String username = "apollo";

    private boolean sameAppId;

    private String authorityKey = "authority-rules";

    private String degradeKey = "degrade-rules";

    private String gatewayApiGroupKey = "gw-api-group-rules";

    private String gatewayFlowKey = "gw-flow-rules";

    private String flowKey = "flow-rules";

    private String paramFlowKey = "param-flow-rules";

    private String systemKey = "system-rules";

    public String getPortalUrl() {
        return portalUrl;
    }

    public void setPortalUrl(String portalUrl) {
        this.portalUrl = portalUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isSameAppId() {
        return sameAppId;
    }

    public void setSameAppId(boolean sameAppId) {
        this.sameAppId = sameAppId;
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
