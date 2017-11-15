/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.rest.v1beta1;

import io.swagger.jaxrs.config.BeanConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@Component
@ApplicationPath("/api/v1beta1")
public class V1Beta1Application extends Application {

    public V1Beta1Application() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("v1beta1");
        beanConfig.setTitle("Syndesis Rest API");
        beanConfig.setSchemes(new String[]{"http", "https"});
        beanConfig.setBasePath("/api/v1beta1");
        beanConfig.setResourcePackage(getClass().getPackage().getName());
        beanConfig.setScan(true);
    }

}
