package com.redhat.ipaas.verifier.v1;

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
