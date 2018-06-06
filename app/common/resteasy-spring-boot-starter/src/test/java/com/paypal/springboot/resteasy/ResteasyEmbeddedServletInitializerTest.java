package com.paypal.springboot.resteasy;

import java.util.Map;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by facarvalho on 11/25/15.
 * @author Fabio Carvalho (facarvalho@paypal.com or fabiocarvalho777@gmail.com)
 */
@ContextConfiguration("classpath:test-config.xml")
public class ResteasyEmbeddedServletInitializerTest extends AbstractTestNGSpringContextTests {

    @Test
    public void postProcessBeanFactory() {
        Map<String, ServletRegistrationBean> servletRegistrationBeans = applicationContext.getBeansOfType(ServletRegistrationBean.class);
        Assert.assertNotNull(servletRegistrationBeans);

        // Although there are 5 sample JAX-RS Application classes, one of them is not annotated with the ApplicationPath annotation!
        Assert.assertEquals(servletRegistrationBeans.size(), 4);

        for(String applicationClassName : servletRegistrationBeans.keySet()) {
            testApplicaton(applicationClassName, servletRegistrationBeans.get(applicationClassName));
        }
    }

    private void testApplicaton(String applicationClassName, ServletRegistrationBean servletRegistrationBean) {
        Assert.assertEquals(applicationClassName, servletRegistrationBean.getServletName());
        Assert.assertTrue(servletRegistrationBean.isAsyncSupported());
        Assert.assertEquals(applicationClassName, servletRegistrationBean.getInitParameters().get("javax.ws.rs.Application"));
        Assert.assertTrue(servletRegistrationBean.isAsyncSupported());
    }

}
