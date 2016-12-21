package com.quancheng.saluki.core.grpc.exception;

public class RpcBizException extends RpcAbstractException {

    private static final long serialVersionUID = -3491276058323309898L;

    public RpcBizException(){
        super(RpcErrorMsgConstant.BIZ_DEFAULT_EXCEPTION);
    }

    public RpcBizException(RpcErrorMsg motanErrorMsg){
        super(motanErrorMsg);
    }

    public RpcBizException(String message){
        super(message, RpcErrorMsgConstant.BIZ_DEFAULT_EXCEPTION);
    }

    public RpcBizException(String message, RpcErrorMsg motanErrorMsg){
        super(message, motanErrorMsg);
    }

    public RpcBizException(String message, Throwable cause){
        super(message, cause, RpcErrorMsgConstant.BIZ_DEFAULT_EXCEPTION);
    }

    public RpcBizException(String message, Throwable cause, RpcErrorMsg motanErrorMsg){
        super(message, cause, motanErrorMsg);
    }

    public RpcBizException(Throwable cause){
        super(cause, RpcErrorMsgConstant.BIZ_DEFAULT_EXCEPTION);
    }

    public RpcBizException(Throwable cause, RpcErrorMsg motanErrorMsg){
        super(cause, motanErrorMsg);
    }
}
