/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.meta.v1;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.swagger.jaxrs.config.BeanConfig;
import org.springframework.stereotype.Component;

/**
 * @author roland
 * @since 28/03/2017
 */
@ApplicationPath("/api/v1")
@Component
@SuppressWarnings("PMD.ShortClassName")
public class V1 extends Application {

    public V1() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("v1");
        beanConfig.setSchemes(new String[]{"http", "https"});
        beanConfig.setBasePath("/api/v1");
        beanConfig.setResourcePackage(getClass().getPackage().getName());
        beanConfig.setScan(true);
    }
}
