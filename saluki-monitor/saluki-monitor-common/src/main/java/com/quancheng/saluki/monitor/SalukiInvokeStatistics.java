package com.quancheng.saluki.monitor;

import java.io.Serializable;

public class SalukiInvokeStatistics implements Serializable {

    private static final long serialVersionUID = -3469058277385014626L;

    private Long              invokeDate;

    private Double            sumconcurrent;

    private Double            sumelapsed;

    private Double            sumsuccess;

    private Double            sumfailure;

    private Double            suminput;

    // 计算
    private Double            tps;

    private Double            kbps;

    private Double            elapsed;

    public Long getInvokeDate() {
        return invokeDate;
    }

    public void setInvokeDate(Long invokeDate) {
        this.invokeDate = invokeDate;
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

    public Double getElapsed() {
        return elapsed;
    }

    public void setElapsed(Double elapsed) {
        this.elapsed = elapsed;
    }

    public Double getSumconcurrent() {
        return sumconcurrent;
    }

    public void setSumconcurrent(Double sumconcurrent) {
        this.sumconcurrent = sumconcurrent;
    }

    public Double getSumelapsed() {
        return sumelapsed;
    }

    public void setSumelapsed(Double sumelapsed) {
        this.sumelapsed = sumelapsed;
    }

    public Double getSumsuccess() {
        return sumsuccess;
    }

    public void setSumsuccess(Double sumsuccess) {
        this.sumsuccess = sumsuccess;
    }

    public Double getSumfailure() {
        return sumfailure;
    }

    public void setSumfailure(Double sumfailure) {
        this.sumfailure = sumfailure;
    }

    public Double getSuminput() {
        return suminput;
    }

    public void setSuminput(Double suminput) {
        this.suminput = suminput;
    }

}
