/**
 * Copyright (c) 2015 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quancheng.saluki.gateway.storage.support;

import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import java.util.List;

/**
 * An general abstraction of persistent storage capable for storing zuul routes. The concrete implementation of this
 * interface will implement provider specific data retrieval.
 *
 * @author Jakub Narloch
 */
public interface ZuulRouteStore {

    /**
     * Retrieves the list of all stored Zuul routes from the persistence storage.
     *
     * @return the list of zuul routes
     */
    List<ZuulProperties.ZuulRoute> findAll();
}
