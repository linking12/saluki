package com.quancheng.saluki.core.grpc;

public class SalukiException extends RuntimeException {

    private static final long serialVersionUID    = 7815426752583648734L;

    public static final int   BIZ_EXCEPTION       = 1;

    public static final int   FRAMEWORK_EXCETPION = 2;

    private int               code;                                      // RpcException不能有子类，异常类型用ErrorCode表示，以便保持兼容。

    public SalukiException(){
        super();
    }

    public SalukiException(String message, Throwable cause){
        super(message, cause);
    }

    public SalukiException(String message){
        super(message);
    }

    public SalukiException(Throwable cause){
        super(cause);
    }

    public SalukiException(int code){
        super();
        this.code = code;
    }

    public SalukiException(int code, String message, Throwable cause){
        super(message, cause);
        this.code = code;
    }

    public SalukiException(int code, String message){
        super(message);
        this.code = code;
    }

    public SalukiException(int code, Throwable cause){
        super(cause);
        this.code = code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public boolean isBiz() {
        return code == BIZ_EXCEPTION;
    }

    public boolean isFramwork() {
        return code == FRAMEWORK_EXCETPION;
    }

}
