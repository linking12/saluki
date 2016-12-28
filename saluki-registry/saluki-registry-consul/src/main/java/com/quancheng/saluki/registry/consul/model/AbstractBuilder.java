package com.quancheng.saluki.registry.consul.model;

import java.util.Map;

abstract class AbstractBuilder {

    private static final String        VARIABLE_START      = "${";
    private static final char          VARIABLE_END        = '}';
    private static final char          DEFAULT_VALUE_START = ':';

    private static Map<String, String> environment         = System.getenv();

    static void setEnvironmentForTesting(Map<String, String> environment) {
        AbstractBuilder.environment = environment;
    }

    protected String substituteEnvironmentVariables(String value) {
        // It might not look pretty, but this is actually about the fastest way to do it!
        final StringBuilder result = new StringBuilder();
        final int length = value.length();
        int index = 0;
        while (index < length) {
            final int start = value.indexOf(VARIABLE_START, index);
            if (start == -1) {
                result.append(value.substring(index));
                return result.toString();
            }
            final int end = value.indexOf(VARIABLE_END, start);
            if (end == -1) {
                result.append(value.substring(index));
                return result.toString();
            }
            if (start > index) {
                result.append(value.substring(index, start));
            }
            String defaultValue = null;
            String variable = value.substring(start + 2, end);
            final int split = variable.indexOf(DEFAULT_VALUE_START);
            if (split != -1) {
                defaultValue = variable.substring(split + 1);
                variable = variable.substring(0, split);
            }
            if (environment.containsKey(variable)) {
                result.append(environment.get(variable));
            } else if (defaultValue != null) {
                result.append(defaultValue);
            }
            index = end + 1;

        }
        return result.toString();
    }
}
