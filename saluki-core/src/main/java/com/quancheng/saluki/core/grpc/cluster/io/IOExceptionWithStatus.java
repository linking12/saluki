
package com.quancheng.saluki.core.grpc.cluster.io;

import java.io.IOException;

import io.grpc.Status;

public class IOExceptionWithStatus extends IOException {

    private static final long serialVersionUID = 1L;
    private final Status      status;

    public IOExceptionWithStatus(String message, Throwable cause, Status status){
        super(message, cause);
        this.status = status;
    }

    public IOExceptionWithStatus(String message, Status status){
        this(message, status.asRuntimeException(), status);
    }

    public Status getStatus() {
        return status;
    }
}
