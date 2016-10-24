package com.quancheng.saluki.core.grpc.exception;

public class RpcErrorMsgConstant {

    // service error status 503
    public static final int         SERVICE_DEFAULT_ERROR_CODE                     = 10001;
    public static final int         SERVICE_REJECT_ERROR_CODE                      = 10002;
    public static final int         SERVICE_TIMEOUT_ERROR_CODE                     = 10003;
    public static final int         SERVICE_TASK_CANCEL_ERROR_CODE                 = 10004;
    // service error status 404
    public static final int         SERVICE_UNFOUND_ERROR_CODE                     = 10101;
    // service error status 403
    public static final int         SERVICE_REQUEST_LENGTH_OUT_OF_LIMIT_ERROR_CODE = 10201;
    // framework error
    public static final int         FRAMEWORK_DEFAULT_ERROR_CODE                   = 20001;
    public static final int         FRAMEWORK_ENCODE_ERROR_CODE                    = 20002;
    public static final int         FRAMEWORK_DECODE_ERROR_CODE                    = 20003;
    public static final int         FRAMEWORK_INIT_ERROR_CODE                      = 20004;
    public static final int         FRAMEWORK_EXPORT_ERROR_CODE                    = 20005;
    public static final int         FRAMEWORK_SERVER_ERROR_CODE                    = 20006;
    public static final int         FRAMEWORK_REFER_ERROR_CODE                     = 20007;
    public static final int         FRAMEWORK_REGISTER_ERROR_CODE                  = 20008;
    // biz exception
    public static final int         BIZ_DEFAULT_ERROR_CODE                         = 30001;
    /**
     * service error start
     **/

    public static final RpcErrorMsg SERVICE_DEFAULT_ERROR                          = new RpcErrorMsg(503,
                                                                                                     SERVICE_DEFAULT_ERROR_CODE,
                                                                                                     "service error");
    public static final RpcErrorMsg SERVICE_REJECT                                 = new RpcErrorMsg(503,
                                                                                                     SERVICE_REJECT_ERROR_CODE,
                                                                                                     "service reject");
    public static final RpcErrorMsg SERVICE_UNFOUND                                = new RpcErrorMsg(404,
                                                                                                     SERVICE_UNFOUND_ERROR_CODE,
                                                                                                     "service unfound");
    public static final RpcErrorMsg SERVICE_TIMEOUT                                = new RpcErrorMsg(503,
                                                                                                     SERVICE_TIMEOUT_ERROR_CODE,
                                                                                                     "service request timeout");
    public static final RpcErrorMsg SERVICE_TASK_CANCEL                            = new RpcErrorMsg(503,
                                                                                                     SERVICE_TASK_CANCEL_ERROR_CODE,
                                                                                                     "service task cancel");
    public static final RpcErrorMsg SERVICE_REQUEST_LENGTH_OUT_OF_LIMIT            = new RpcErrorMsg(403,
                                                                                                     SERVICE_REQUEST_LENGTH_OUT_OF_LIMIT_ERROR_CODE,
                                                                                                     "servier requset data length over of limit");
    /**
     * framework error start
     **/
    public static final RpcErrorMsg FRAMEWORK_DEFAULT_ERROR                        = new RpcErrorMsg(503,
                                                                                                     FRAMEWORK_DEFAULT_ERROR_CODE,
                                                                                                     "framework default error");

    /** service error end **/
    public static final RpcErrorMsg FRAMEWORK_ENCODE_ERROR                         = new RpcErrorMsg(503,
                                                                                                     FRAMEWORK_ENCODE_ERROR_CODE,
                                                                                                     "framework encode error");
    public static final RpcErrorMsg FRAMEWORK_DECODE_ERROR                         = new RpcErrorMsg(503,
                                                                                                     FRAMEWORK_DECODE_ERROR_CODE,
                                                                                                     "framework decode error");
    public static final RpcErrorMsg FRAMEWORK_INIT_ERROR                           = new RpcErrorMsg(500,
                                                                                                     FRAMEWORK_INIT_ERROR_CODE,
                                                                                                     "framework init error");
    public static final RpcErrorMsg FRAMEWORK_EXPORT_ERROR                         = new RpcErrorMsg(503,
                                                                                                     FRAMEWORK_EXPORT_ERROR_CODE,
                                                                                                     "framework export error");
    public static final RpcErrorMsg FRAMEWORK_REFER_ERROR                          = new RpcErrorMsg(503,
                                                                                                     FRAMEWORK_REFER_ERROR_CODE,
                                                                                                     "framework refer error");
    /**
     * biz error start
     **/
    public static final RpcErrorMsg BIZ_DEFAULT_EXCEPTION                          = new RpcErrorMsg(503,
                                                                                                     BIZ_DEFAULT_ERROR_CODE,
                                                                                                     "provider error");

    /** framework error end **/

    private RpcErrorMsgConstant(){
    }
    /** biz error end **/
}
