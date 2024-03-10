package com.alibaba.csp.sentinel.dashboard.rule.apollo;

public abstract class ApolloConfigUtil {

    private static final String FORMAT = "%s-%s";

    public static String formatKey(boolean execute, String prefix, String raw) {
        return execute ? String.format(FORMAT, prefix, raw) : raw;
    }
}
