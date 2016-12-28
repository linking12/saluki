package com.quancheng.saluki.core.grpc.service;

import com.quancheng.saluki.core.common.GrpcURL;

public interface MonitorService {

    String APPLICATION    = "application";

    String INTERFACE      = "interface";

    String METHOD         = "method";

    String GROUP          = "group";

    String VERSION        = "version";

    String CONSUMER       = "consumer";

    String PROVIDER       = "provider";

    String TIMESTAMP      = "timestamp";

    String SUCCESS        = "success";

    String FAILURE        = "failure";

    String ELAPSED        = "elapsed";

    String CONCURRENT     = "concurrent";

    String TRACEID        = "traceId";

    String INPUT          = "input";

    String OUTPUT         = "output";

    String MAX_INPUT      = "max.input";

    String MAX_OUTPUT     = "max.output";

    String MAX_ELAPSED    = "max.elapsed";

    String MAX_CONCURRENT = "max.concurrent";

    /**
     * 监控数据采集. 1.
     * 支持调用次数统计：count://host/interface?application=foo&method=foo&provider=10.20.153.11:20880&success=12&failure=2&elapsed=135423423
     * 1.1 host,application,interface,group,version,method 记录监控来源主机，应用，接口，方法信息。 1.2
     * 如果是消费者发送的数据，加上provider地址参数，反之，加上来源consumer地址参数。 1.3 success,faulure,elapsed
     * 记录距上次采集，调用的成功次数，失败次数，成功调用总耗时，平均时间将用总耗时除以成功次数。
     * 
     * @param statistics
     */
    void collect(GrpcURL statistics);

}
