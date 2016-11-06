package com.quancheng.saluki.core.grpc.exception;

import java.io.Serializable;

/**
 * @author maijunsheng
 * @version 创建时间：2013-5-30
 */
public class RpcErrorMsg implements Serializable {

    private static final long serialVersionUID = 4909459500370103048L;

    private int               status;
    private int               errorcode;
    private String            message;

    public RpcErrorMsg(int status, int errorcode, String message){
        this.status = status;
        this.errorcode = errorcode;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public int getErrorCode() {
        return errorcode;
    }

    public String getMessage() {
        return message;
    }

}
