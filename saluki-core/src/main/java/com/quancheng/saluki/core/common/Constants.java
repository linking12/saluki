package com.quancheng.saluki.core.common;

import java.util.regex.Pattern;

public class Constants {

    public static final Pattern COMMA_SPLIT_PATTERN           = Pattern.compile("\\s*[,]+\\s*");

    public static final String  INTERFACE_KEY                 = "interface";
    public static final String  INTERFACECLASS_KEY            = "interfaceClass";
    public static final String  GRPC_STUB_KEY                 = "grpcstub";
    public static final String  GENERIC_KEY                   = "generic";
    public static final String  GROUP_KEY                     = "group";
    public static final String  VERSION_KEY                   = "version";
    public static final String  REGISTRY_RPC_PORT_KEY         = "registryrpcport";
    public static final String  HTTP_PORT_KEY                 = "httpport";
    public static final String  MONITOR_INTERVAL              = "monitorinterval";
    public static final String  APPLICATION_NAME              = "application";
    public static final String  TIMEOUT                       = "timeout";
    public static final String  DEFAULT_GROUP                 = "Default";
    public static final String  DEFAULT_VERSION               = "1.0.0";
    public static final String  LOCALHOST_KEY                 = "localhost";
    public static final String  ANYHOST_KEY                   = "anyhost";
    public static final String  METHODS_KEY                   = "methods";
    public static final String  METHOD_KEY                    = "method";
    public static final String  ARG_KEY                       = "arg";
    public static final String  METHOD_RETRY_KEY              = "retries";
    public static final String  ANYHOST_VALUE                 = "0.0.0.0";

    public static final String  REGISTRY_RETRY_PERIOD_KEY     = "retry.period";
    public static final int     DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;
    public static final String  ENABLED_KEY                   = "enabled";
    public static final String  DISABLED_KEY                  = "disabled";

    public static final String  CONSUL_SERVICE_PRE            = "saluki_";
    public static final String  PATH_SEPARATOR                = "/";
    public static final String  PROVIDERS_CATEGORY            = "providers";
    public static final String  CONSUMERS_CATEGORY            = "consumers";

    public static final String  REGISTRY_PROTOCOL             = "registry";
    public static final String  REMOTE_PROTOCOL               = "grpc";
    public static final String  MONITOR_PROTOCOL              = "monitor";

    public static final String  ASYNC_KEY                     = "async";
    public static final int     RPCTYPE_ASYNC                 = 1;
    public static final int     RPCTYPE_BLOCKING              = 2;
    public static final int     RPC_ASYNC_DEFAULT_TIMEOUT     = 10000;

    public static final String  REMOTE_ADDRESS                = "remote";

}
