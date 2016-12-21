package com.quancheng.saluki.core.grpc.exception;

public class RpcFrameworkException extends RpcAbstractException {

    private static final long serialVersionUID = -1638857395789735293L;

    public RpcFrameworkException(){
        super(RpcErrorMsgConstant.FRAMEWORK_DEFAULT_ERROR);
    }

    public RpcFrameworkException(RpcErrorMsg motanErrorMsg){
        super(motanErrorMsg);
    }

    public RpcFrameworkException(String message){
        super(message, RpcErrorMsgConstant.FRAMEWORK_DEFAULT_ERROR);
    }

    public RpcFrameworkException(String message, RpcErrorMsg motanErrorMsg){
        super(message, motanErrorMsg);
    }

    public RpcFrameworkException(String message, Throwable cause){
        super(message, cause, RpcErrorMsgConstant.FRAMEWORK_DEFAULT_ERROR);
    }

    public RpcFrameworkException(String message, Throwable cause, RpcErrorMsg motanErrorMsg){
        super(message, cause, motanErrorMsg);
    }

    public RpcFrameworkException(Throwable cause){
        super(cause, RpcErrorMsgConstant.FRAMEWORK_DEFAULT_ERROR);
    }

    public RpcFrameworkException(Throwable cause, RpcErrorMsg motanErrorMsg){
        super(cause, motanErrorMsg);
    }

}
