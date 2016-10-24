package com.quancheng.saluki.core.grpc.client.ha.internal;

import java.io.IOException;

public class RetriesExhaustedException extends IOException {

    private static final long serialVersionUID = 6905598607595217072L;

    public RetriesExhaustedException(String message, Throwable cause){
        super(message, cause);
    }
}
