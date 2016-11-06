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
import java.util.Set;

/**
 * Dubbo Application Entity
 *
 * @author Zhiguo.Chen <me@chenzhiguo.cn> Created on 15/6/27.
 */
public class SalukiApplication implements Serializable {

    private String      name;

    private Set<String> providers;

    private Set<String> consumers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getProviders() {
        return providers;
    }

    public void setProviders(Set<String> providers) {
        this.providers = providers;
    }

    public Set<String> getConsumers() {
        return consumers;
    }

    public void setConsumers(Set<String> consumers) {
        this.consumers = consumers;
    }

}
