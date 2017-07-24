package com.quancheng.saluki.core.grpc.exception;

public class RpcValidatorException extends RpcAbstractException {

    private static final long serialVersionUID = -3491276058323309898L;

    public RpcValidatorException(){
        super(RpcErrorMsgConstant.REQUEST_ARG_VALIDATE_EXCEPTION);
    }

    public RpcValidatorException(RpcErrorMsg motanErrorMsg){
        super(motanErrorMsg);
    }

    public RpcValidatorException(String message){
        super(message, RpcErrorMsgConstant.REQUEST_ARG_VALIDATE_EXCEPTION);
    }

    public RpcValidatorException(String message, RpcErrorMsg motanErrorMsg){
        super(message, motanErrorMsg);
    }

    public RpcValidatorException(String message, Throwable cause){
        super(message, cause, RpcErrorMsgConstant.REQUEST_ARG_VALIDATE_EXCEPTION);
    }

    public RpcValidatorException(String message, Throwable cause, RpcErrorMsg motanErrorMsg){
        super(message, cause, motanErrorMsg);
    }

    public RpcValidatorException(Throwable cause){
        super(cause, RpcErrorMsgConstant.REQUEST_ARG_VALIDATE_EXCEPTION);
    }

    public RpcValidatorException(Throwable cause, RpcErrorMsg motanErrorMsg){
        super(cause, motanErrorMsg);
    }
}
