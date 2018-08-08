/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mediation.security.vault;

import org.apache.synapse.registry.Registry;
import org.wso2.carbon.base.api.ServerConfigurationService;

public class SecurityServiceHolder {

	private Registry registry;

	private ServerConfigurationService serverConfigurationService;

	private static final SecurityServiceHolder INSTANCE = new SecurityServiceHolder();

	private SecurityServiceHolder() {

	}

	public static SecurityServiceHolder getInstance() {
		return INSTANCE;
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	public ServerConfigurationService getServerConfigurationService() {
		return serverConfigurationService;
	}

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

}
