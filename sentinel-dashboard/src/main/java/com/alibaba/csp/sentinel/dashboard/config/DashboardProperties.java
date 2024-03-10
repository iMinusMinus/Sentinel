package com.alibaba.csp.sentinel.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sentinel.dashboard")
public class DashboardProperties {

    static final int MIN_REMOVE_APP_NO_MACHINE_MS = 120000;

    static final int MIN_MACHINE_HEALTHY_TIMEOUT_MS = 30000;

    static final int MIN_AUTO_REMOVE_MACHINE_MILLIS = 300000;

    static final int DEFAULT_UNHEALTHY_MACHINE_MS = 60000;

    private AuthConfig auth = new AuthConfig();

    private AppConfig app = new AppConfig();

    /**
     * Remove application when it has no healthy machines after specific period in millisecond.
     */
    private int removeAppNoMachineMillis;

    /**
     * Timeout
     */
    private int unhealthyMachineMillis = DEFAULT_UNHEALTHY_MACHINE_MS;

    /**
     * Auto remove unhealthy machine after specific period in millisecond.
     */
    private int autoRemoveMachineMillis;

    public AuthConfig getAuth() {
        return auth;
    }

    public void setAuth(AuthConfig auth) {
        this.auth = auth;
    }

    public AppConfig getApp() {
        return app;
    }

    public void setApp(AppConfig app) {
        this.app = app;
    }

    public int getRemoveAppNoMachineMillis() {
        return Math.max(removeAppNoMachineMillis, MIN_REMOVE_APP_NO_MACHINE_MS);
    }

    public void setRemoveAppNoMachineMillis(int removeAppNoMachineMillis) {
        this.removeAppNoMachineMillis = removeAppNoMachineMillis;
    }

    public int getUnhealthyMachineMillis() {
        return Math.max(unhealthyMachineMillis, MIN_MACHINE_HEALTHY_TIMEOUT_MS);
    }

    public void setUnhealthyMachineMillis(int unhealthyMachineMillis) {
        this.unhealthyMachineMillis = unhealthyMachineMillis;
    }

    public int getAutoRemoveMachineMillis() {
        return Math.max(autoRemoveMachineMillis, MIN_AUTO_REMOVE_MACHINE_MILLIS);
    }

    public void setAutoRemoveMachineMillis(int autoRemoveMachineMillis) {
        this.autoRemoveMachineMillis = autoRemoveMachineMillis;
    }

    public static class AuthConfig {

        private boolean enabled = true;

        /**
         * Login username
         */
        private String username;

        /**
         * Login password
         */
        private String password;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
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
    }

    public static class AppConfig {

        static final int MIN_HIDE_APP_NO_MACHINE_MS = 60000;

        /**
         * Hide application name in sidebar when it has no healthy machines after specific period in millisecond.
         */
        private int hideAppNoMachineMillis;

        public int getHideAppNoMachineMillis() {
            return Math.max(hideAppNoMachineMillis, MIN_HIDE_APP_NO_MACHINE_MS);
        }

        public void setHideAppNoMachineMillis(int hideAppNoMachineMillis) {
            this.hideAppNoMachineMillis = hideAppNoMachineMillis;
        }
    }
}
