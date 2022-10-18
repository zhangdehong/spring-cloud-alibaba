/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos;

import java.util.Objects;

import com.alibaba.cloud.nacos.diagnostics.analyzer.NacosConnectionFailureException;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zkzlx
 */
public class NacosConfigManager {

	private static final Logger log = LoggerFactory.getLogger(NacosConfigManager.class);

	private static ConfigService service = null;

    // nacos 的配置属性
	private NacosConfigProperties nacosConfigProperties;

	public NacosConfigManager(NacosConfigProperties nacosConfigProperties) {
		this.nacosConfigProperties = nacosConfigProperties;
		// Compatible with older code in NacosConfigProperties,It will be deleted in the
		// future.
        // nacos配置里面提供了nacos地址、领域模型、认证信息
		createConfigService(nacosConfigProperties);
	}

	/**
	 * Compatible with old design,It will be perfected in the future.
	 */
    // 直接返回Nacoslient接口实例
	static ConfigService createConfigService(
			NacosConfigProperties nacosConfigProperties) {
		if (Objects.isNull(service)) {
            // 创建过程同步处理 一个nacos客户端里面应该只有一个ConfigService实例 多个是没有任何意义的
			synchronized (NacosConfigManager.class) {
				try {
                    // 饿汉式单例模式  双重锁检测机制
					if (Objects.isNull(service)) {
						service = NacosFactory.createConfigService(
                                // nacos properties转换为properties
								nacosConfigProperties.assembleConfigServiceProperties());
					}
				}
				catch (NacosException e) {
					log.error(e.getMessage());
					throw new NacosConnectionFailureException(
							nacosConfigProperties.getServerAddr(), e.getMessage(), e);
				}
			}
		}
		return service;
	}

	public ConfigService getConfigService() {
		if (Objects.isNull(service)) {
			createConfigService(this.nacosConfigProperties);
		}
		return service;
	}

	public NacosConfigProperties getNacosConfigProperties() {
		return nacosConfigProperties;
	}

}
