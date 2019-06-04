/*
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.endpoint.ui.endpoints.loadbalance;

import org.wso2.carbon.endpoint.ui.endpoints.Endpoint;
import org.wso2.carbon.endpoint.ui.endpoints.EndpointService;

public class LoadBalanceEndpointService implements EndpointService {

    public Endpoint getEndpoint() {
        return new LoadBalanceEndpoint();
    }

    public String getType() {
        return "loadbalance";
    }

    public String getUIPageName() {
        return "loadBalance";
    }

    public String getDescription() {
        return "定义复制服务的端点组。传入请求将以循环方式定向到这些端点。这些端点也自动处理故障转移情况";
    }

    public String getDisplayName() {
        return "负载均衡端点";
    }

    public boolean isStatisticsAvailable() {
        return false;
    }

    public boolean canAddAsChild() {
        return true;
    }

    public boolean canAddAsTemplate() {
        return false;
    }

    public boolean isChildEndpointFormAvailable() {
        return true;
    }

}
