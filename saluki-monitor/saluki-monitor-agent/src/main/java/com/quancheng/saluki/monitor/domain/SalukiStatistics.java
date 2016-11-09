/**
 * Copyright 2006-2015 handu.com
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quancheng.saluki.monitor.domain;

import java.io.Serializable;

public class SalukiStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            method;

    private double            success;

    private double            failure;

    private double            avgElapsed;

    private double            maxConcurrent;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public double getSuccess() {
        return success;
    }

    public void setSuccess(double success) {
        this.success = success;
    }

    public double getFailure() {
        return failure;
    }

    public void setFailure(double failure) {
        this.failure = failure;
    }

    public double getAvgElapsed() {
        return avgElapsed;
    }

    public void setAvgElapsed(double avgElapsed) {
        this.avgElapsed = avgElapsed;
    }

    public double getMaxConcurrent() {
        return maxConcurrent;
    }

    public void setMaxConcurrent(double maxConcurrent) {
        this.maxConcurrent = maxConcurrent;
    }

}
