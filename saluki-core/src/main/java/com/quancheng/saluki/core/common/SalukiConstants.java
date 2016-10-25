package com.quancheng.saluki.core.common;

import java.util.regex.Pattern;

import com.quancheng.saluki.core.grpc.utils.Marshallers;

import io.grpc.Attributes;
import io.grpc.Metadata;

public class SalukiConstants {

    public static final Pattern COMMA_SPLIT_PATTERN           = Pattern.compile("\\s*[,]+\\s*");

    public static final String  INTERFACE_KEY                 = "interface";
    public static final String  INTERFACECLASS_KEY            = "interfaceClass";
    public static final String  GRPC_STUB_KEY                 = "grpcstub";
    public static final String  GENERIC_KEY                   = "generic";
    public static final String  GROUP_KEY                     = "group";
    public static final String  VERSION_KEY                   = "version";
    public static final String  RPCTIMEOUT_KEY                = "timeout";
    public static final String  DEFAULT_GROUP                 = "Default";
    public static final String  DEFAULT_VERSION               = "1.0.0";
    public static final String  LOCALHOST_KEY                 = "localhost";
    public static final String  ANYHOST_KEY                   = "anyhost";
    public static final String  ANYHOST_VALUE                 = "0.0.0.0";

    public static final String  REGISTRY_RETRY_PERIOD_KEY     = "retry.period";
    public static final int     DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;
    public static final String  ENABLED_KEY                   = "enabled";
    public static final String  DISABLED_KEY                  = "disabled";

    public final static String  PATH_SEPARATOR                = "/";
    public static final String  PROVIDERS_CATEGORY            = "providers";
    public static final String  CONSUMERS_CATEGORY            = "consumers";

    public static final String  REGISTRY_PROTOCOL             = "registry";
    public static final String  DEFATULT_PROTOCOL             = "Grpc";

    public static final String  RPCTYPE_KEY                   = "syc";
    public static final int     RPCTYPE_ASYNC                 = 1;
    public static final int     RPCTYPE_BLOCKING              = 2;

    public static final int     DEFAULT_TIMEOUT               = 60;
    public static final boolean DEFAULT_GENERIC               = false;
    public static final String  GRPC_IN_LOCAL_PROCESS         = "LocalProcess";

    public static final String  REMOTE_ADDRESS                = "RmoteAddress";

}
