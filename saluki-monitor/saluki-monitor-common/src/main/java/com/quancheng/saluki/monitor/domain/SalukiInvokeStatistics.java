
package com.quancheng.saluki.monitor.domain;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class SalukiInvokeStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            id;

    private Date              invokeDate;

    private String            service;

    private String            method;

    private String            consumer;

    private String            provider;

    private String            type;

    private int               concurrent;

    private int               maxInput;

    private int               maxOutput;

    private double            maxElapsed;

    private int               maxConcurrent;

    // 总调用成功数
    private int               success;

    // 总调用失败数
    private int               failure;

    // 平均数据请求量
    private double            input;

    // 平均数据返回量
    private double            output;

    // 平均耗时
    private double            elapsed;

    // 处理请求的速度
    private double            tps;

    // 网络传输速度
    private double            kbps;

    public Date getInvokeDate() {
        return invokeDate;
    }

    public void setInvokeDate(Date invokeDate) {
        this.invokeDate = invokeDate;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getType() {
        if (StringUtils.isEmpty(type)) {
            return "provider";
        }
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public double getElapsed() {
        return elapsed;
    }

    public void setElapsed(double elapsed) {
        this.elapsed = elapsed;
    }

    public int getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(int concurrent) {
        this.concurrent = concurrent;
    }

    public int getMaxInput() {
        return maxInput;
    }

    public void setMaxInput(int maxInput) {
        this.maxInput = maxInput;
    }

    public int getMaxOutput() {
        return maxOutput;
    }

    public void setMaxOutput(int maxOutput) {
        this.maxOutput = maxOutput;
    }

    public double getMaxElapsed() {
        return maxElapsed;
    }

    public void setMaxElapsed(double maxElapsed) {
        this.maxElapsed = maxElapsed;
    }

    public int getMaxConcurrent() {
        return maxConcurrent;
    }

    public void setMaxConcurrent(int maxConcurrent) {
        this.maxConcurrent = maxConcurrent;
    }

    public double getInput() {
        return input;
    }

    public void setInput(double input) {
        this.input = input;
    }

    public double getOutput() {
        return output;
    }

    public void setOutput(double output) {
        this.output = output;
    }

    public double getTps() {
        return tps;
    }

    public void setTps(double tps) {
        this.tps = tps;
    }

    public double getKbps() {
        return kbps;
    }

    public void setKbps(double kbps) {
        this.kbps = kbps;
    }

    public void setId(String id) {
        this.id = id;
    }

}
