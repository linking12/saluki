package com.quancheng.saluki.domain;

import java.io.Serializable;
import java.util.Date;

public class GrpcnvokeStatistics implements Serializable {

    private static final long serialVersionUID = -2143317389694773902L;

    private Date              invokeDate;

    private int               sumConcurrent;

    private int               sumSuccess;

    private int               sumFailure;

    private double            sumInput;

    private double            sumOutput;

    private double            sumElapsed;

    private int               maxInput;

    private int               maxOutput;

    private double            maxElapsed;

    private int               maxConcurrent;

    // 计算
    private Double            tps;

    private Double            kbps;

    public int getSumConcurrent() {
        return sumConcurrent;
    }

    public void setSumConcurrent(int sumConcurrent) {
        this.sumConcurrent = sumConcurrent;
    }

    public int getSumSuccess() {
        return sumSuccess;
    }

    public void setSumSuccess(int sumSuccess) {
        this.sumSuccess = sumSuccess;
    }

    public int getSumFailure() {
        return sumFailure;
    }

    public void setSumFailure(int sumFailure) {
        this.sumFailure = sumFailure;
    }

    public double getSumInput() {
        return sumInput;
    }

    public void setSumInput(double sumInput) {
        this.sumInput = sumInput;
    }

    public double getSumOutput() {
        return sumOutput;
    }

    public void setSumOutput(double sumOutput) {
        this.sumOutput = sumOutput;
    }

    public double getSumElapsed() {
        return sumElapsed;
    }

    public void setSumElapsed(double sumElapsed) {
        this.sumElapsed = sumElapsed;
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

    public Double getTps() {
        return tps;
    }

    public void setTps(Double tps) {
        this.tps = tps;
    }

    public Double getKbps() {
        return kbps;
    }

    public void setKbps(Double kbps) {
        this.kbps = kbps;
    }

    public Date getInvokeDate() {
        return invokeDate;
    }

    public void setInvokeDate(Date invokeDate) {
        this.invokeDate = invokeDate;
    }

}
