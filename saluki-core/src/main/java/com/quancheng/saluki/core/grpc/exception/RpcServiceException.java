package com.quancheng.saluki.core.grpc.exception;

public class RpcServiceException extends RpcAbstractException {

    private static final long serialVersionUID = -3491276058323309898L;

    public RpcServiceException(){
        super(RpcErrorMsgConstant.SERVICE_DEFAULT_ERROR);
    }

    public RpcServiceException(RpcErrorMsg motanErrorMsg){
        super(motanErrorMsg);
    }

    public RpcServiceException(String message){
        super(message, RpcErrorMsgConstant.SERVICE_DEFAULT_ERROR);
    }

    public RpcServiceException(String message, RpcErrorMsg motanErrorMsg){
        super(message, motanErrorMsg);
    }

    public RpcServiceException(String message, Throwable cause){
        super(message, cause, RpcErrorMsgConstant.SERVICE_DEFAULT_ERROR);
    }

    public RpcServiceException(String message, Throwable cause, RpcErrorMsg motanErrorMsg){
        super(message, cause, motanErrorMsg);
    }

    public RpcServiceException(Throwable cause){
        super(cause, RpcErrorMsgConstant.SERVICE_DEFAULT_ERROR);
    }

    public RpcServiceException(Throwable cause, RpcErrorMsg motanErrorMsg){
        super(cause, motanErrorMsg);
    }
}
