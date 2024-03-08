package com.alibaba.csp.sentinel.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sentinel.dashboard")
public class DashboardProperties {

    private static final int MIN_REMOVE_APP_NO_MATCH_MS = 120000;

    private static final int MIN_MACHINE_HEALTHY_TIMEOUT_MS = 30000;

    private static final int MIN_AUTO_REMOVE_MACHINE_MILLIS = 300000;

    private AuthProperties auth;

    private AppProperties app;

    /**
     * Remove application when it has no healthy machines after specific period in millisecond.
     */
    private int removeAppNoMachineMillis;

    /**
     * Timeout
     */
    private int unhealthyMachineMillis = 60000;

    /**
     * Auto remove unhealthy machine after specific period in millisecond.
     */
    private int autoRemoveMachineMillis;

    public AuthProperties getAuth() {
        return auth;
    }

    public void setAuth(AuthProperties auth) {
        this.auth = auth;
    }

    public AppProperties getApp() {
        return app;
    }

    public void setApp(AppProperties app) {
        this.app = app;
    }

    public int getRemoveAppNoMachineMillis() {
        return removeAppNoMachineMillis >= MIN_REMOVE_APP_NO_MATCH_MS ? removeAppNoMachineMillis : MIN_REMOVE_APP_NO_MATCH_MS;
    }

    public void setRemoveAppNoMachineMillis(int removeAppNoMachineMillis) {
        this.removeAppNoMachineMillis = removeAppNoMachineMillis;
    }

    public int getUnhealthyMachineMillis() {
        return unhealthyMachineMillis >= MIN_MACHINE_HEALTHY_TIMEOUT_MS ? unhealthyMachineMillis : MIN_MACHINE_HEALTHY_TIMEOUT_MS;
    }

    public void setUnhealthyMachineMillis(int unhealthyMachineMillis) {
        this.unhealthyMachineMillis = unhealthyMachineMillis;
    }

    public int getAutoRemoveMachineMillis() {
        return autoRemoveMachineMillis >= MIN_AUTO_REMOVE_MACHINE_MILLIS ? autoRemoveMachineMillis : MIN_AUTO_REMOVE_MACHINE_MILLIS;
    }

    public void setAutoRemoveMachineMillis(int autoRemoveMachineMillis) {
        this.autoRemoveMachineMillis = autoRemoveMachineMillis;
    }
}
