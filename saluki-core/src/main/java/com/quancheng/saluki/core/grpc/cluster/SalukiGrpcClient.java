package com.quancheng.saluki.core.grpc.cluster;

import java.util.List;

import com.google.common.util.concurrent.ListenableFuture;
import com.quancheng.saluki.core.grpc.cluster.async.SalukiAsyncRpc;

public interface SalukiGrpcClient {
    
    
    

    public <ReqT, RespT> ListenableFuture<List<RespT>> getStreamingFuture(ReqT request,
                                                                          SalukiAsyncRpc<ReqT, RespT> rpc);

    public <ReqT, RespT> List<RespT> getBlockingStreamingResult(ReqT request, SalukiAsyncRpc<ReqT, RespT> rpc);

    public <ReqT, RespT> ListenableFuture<RespT> getUnaryFuture(ReqT request, SalukiAsyncRpc<ReqT, RespT> rpc,
                                                                int retryTimes);

    public <ReqT, RespT> RespT getBlockingUnaryResult(ReqT request, SalukiAsyncRpc<ReqT, RespT> rpc);

}
