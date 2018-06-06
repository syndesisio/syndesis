package com.paypal.springboot.resteasy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

import com.paypal.springboot.resteasy.sample.TestApplication1;
import com.paypal.springboot.resteasy.sample.TestApplication2;
import com.paypal.springboot.resteasy.sample.TestApplication3;
import com.paypal.springboot.resteasy.sample.TestApplication4;
import com.paypal.springboot.resteasy.sample.TestApplication5;
import com.paypal.springboot.resteasy.sample.TestResource1;
import com.paypal.springboot.resteasy.sample.TestResource2;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Created by facarvalho on 7/19/16.
 * @author Fabio Carvalho (facarvalho@paypal.com or fabiocarvalho777@gmail.com)
 */
@PrepareForTest(AutoConfigurationPackages.class)
@MockPolicy(Slf4jMockPolicy.class)
public class JaxrsAppRegistrationTest extends PowerMockTestCase {

    private static final String DEFINITION_PROPERTY = "resteasy.jaxrs.app.registration";
    private static final String APP_CLASSES_PROPERTY = "resteasy.jaxrs.app.classes";
    private static final String APP_CLASSES_PROPERTY_LEGACY = "resteasy.jaxrs.app";

    private static Set<Class<?>> allPossibleAppClasses;

    static {
        Set<Class<?>> tmpAllPossibleAppClasses = new HashSet<>();

        tmpAllPossibleAppClasses.add(TestApplication1.class);
        tmpAllPossibleAppClasses.add(TestApplication2.class);
        tmpAllPossibleAppClasses.add(TestApplication3.class);
        tmpAllPossibleAppClasses.add(TestApplication4.class);
        tmpAllPossibleAppClasses.add(TestApplication5.class);
        tmpAllPossibleAppClasses.add(Application.class);

        allPossibleAppClasses = Collections.unmodifiableSet(tmpAllPossibleAppClasses);
    }

    @BeforeMethod
    public void beforeTest() {
        PowerMockito.mockStatic(AutoConfigurationPackages.class);
        List<String> packages = new ArrayList<String>();
        packages.add("com.paypal.springboot.resteasy.sample");
        PowerMockito.when(AutoConfigurationPackages.get(any(BeanFactory.class))).thenReturn(packages);
    }

    @Test
    public void nullTest() {
        ConfigurableEnvironment configurableEnvironmentMock = mock(ConfigurableEnvironment.class);
        when(configurableEnvironmentMock.getProperty(DEFINITION_PROPERTY)).thenReturn(null);

        Set<Class<?>> expectedRegisteredAppClasses = new HashSet<>();
        expectedRegisteredAppClasses.add(TestApplication1.class);
        expectedRegisteredAppClasses.add(TestApplication2.class);
        expectedRegisteredAppClasses.add(TestApplication4.class);
        expectedRegisteredAppClasses.add(TestApplication5.class);

        test(configurableEnvironmentMock, expectedRegisteredAppClasses);
    }

    @Test
    public void autoTest() {
        ConfigurableEnvironment configurableEnvironmentMock = mock(ConfigurableEnvironment.class);
        when(configurableEnvironmentMock.getProperty(DEFINITION_PROPERTY)).thenReturn("auto");

        Set<Class<?>> expectedRegisteredAppClasses = new HashSet<>();
        expectedRegisteredAppClasses.add(TestApplication1.class);
        expectedRegisteredAppClasses.add(TestApplication2.class);
        expectedRegisteredAppClasses.add(TestApplication4.class);
        expectedRegisteredAppClasses.add(TestApplication5.class);

        test(configurableEnvironmentMock, expectedRegisteredAppClasses);
    }

    @Test
    public void beansTest() {
        ConfigurableEnvironment configurableEnvironmentMock = mock(ConfigurableEnvironment.class);
        when(configurableEnvironmentMock.getProperty(DEFINITION_PROPERTY)).thenReturn("beans");

        Set<Class<?>> expectedRegisteredAppClasses = new HashSet<>();
        expectedRegisteredAppClasses.add(TestApplication1.class);
        expectedRegisteredAppClasses.add(TestApplication4.class);

        test(configurableEnvironmentMock, expectedRegisteredAppClasses);
    }

    @Test
    public void propertyTest() {
        ConfigurableEnvironment configurableEnvironmentMock = mock(ConfigurableEnvironment.class);
        when(configurableEnvironmentMock.getProperty(DEFINITION_PROPERTY)).thenReturn("property");
        when(configurableEnvironmentMock.getProperty(APP_CLASSES_PROPERTY)).thenReturn("com.paypal.springboot.resteasy.sample.TestApplication3, com.paypal.springboot.resteasy.sample.TestApplication4,com.paypal.springboot.resteasy.sample.TestApplication2");

        Set<Class<?>> expectedRegisteredAppClasses = new HashSet<>();
        expectedRegisteredAppClasses.add(TestApplication2.class);
        expectedRegisteredAppClasses.add(TestApplication4.class);

        test(configurableEnvironmentMock, expectedRegisteredAppClasses);
    }

    @Test
    public void legacyPropertyTest() {
        ConfigurableEnvironment configurableEnvironmentMock = mock(ConfigurableEnvironment.class);
        when(configurableEnvironmentMock.getProperty(DEFINITION_PROPERTY)).thenReturn("property");
        when(configurableEnvironmentMock.getProperty(APP_CLASSES_PROPERTY_LEGACY)).thenReturn("com.paypal.springboot.resteasy.sample.TestApplication3, com.paypal.springboot.resteasy.sample.TestApplication4,com.paypal.springboot.resteasy.sample.TestApplication2");

        Set<Class<?>> expectedRegisteredAppClasses = new HashSet<>();
        expectedRegisteredAppClasses.add(TestApplication2.class);
        expectedRegisteredAppClasses.add(TestApplication4.class);

        test(configurableEnvironmentMock, expectedRegisteredAppClasses);
    }

    @Test
    public void scanningTest() {
        ConfigurableEnvironment configurableEnvironmentMock = mock(ConfigurableEnvironment.class);
        when(configurableEnvironmentMock.getProperty(DEFINITION_PROPERTY)).thenReturn("scanning");

        Set<Class<?>> expectedRegisteredAppClasses = new HashSet<>();
        expectedRegisteredAppClasses.add(TestApplication1.class);
        expectedRegisteredAppClasses.add(TestApplication2.class);
        expectedRegisteredAppClasses.add(TestApplication4.class);
        expectedRegisteredAppClasses.add(TestApplication5.class);

        test(configurableEnvironmentMock, expectedRegisteredAppClasses);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = "Property " + DEFINITION_PROPERTY +
                    " has not been properly set, value blah is invalid. JAX-RS Application classes registration is being set to AUTO.")
    public void invalidRegistrationTest() {
        ConfigurableEnvironment configurableEnvironmentMock = mock(ConfigurableEnvironment.class);
        when(configurableEnvironmentMock.getProperty(DEFINITION_PROPERTY)).thenReturn("blah");

        ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
        when(beanFactory.getBean(ConfigurableEnvironment.class)).thenReturn(configurableEnvironmentMock);

        ResteasyEmbeddedServletInitializer resteasyEmbeddedServletInitializer = new ResteasyEmbeddedServletInitializer();
        resteasyEmbeddedServletInitializer.postProcessBeanFactory(beanFactory);
    }

    @Test(expectedExceptions = BeansException.class)
    public void classNotFoundTest() {
        ConfigurableEnvironment configurableEnvironmentMock = mock(ConfigurableEnvironment.class);
        when(configurableEnvironmentMock.getProperty(DEFINITION_PROPERTY)).thenReturn("property");
        when(configurableEnvironmentMock.getProperty(APP_CLASSES_PROPERTY)).thenReturn("com.paypal.springboot.resteasy.sample.TestApplication3, com.paypal.springboot.resteasy.sample.TestApplication4,com.paypal.springboot.resteasy.sample.TestApplication9");

        ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class);
        when(beanFactory.getBean(ConfigurableEnvironment.class)).thenReturn(configurableEnvironmentMock);

        ResteasyEmbeddedServletInitializer resteasyEmbeddedServletInitializer = new ResteasyEmbeddedServletInitializer();
        resteasyEmbeddedServletInitializer.postProcessBeanFactory(beanFactory);
    }

    @Test
    public void testPropertyNoApps() {
        ConfigurableEnvironment configurableEnvironmentMock = mock(ConfigurableEnvironment.class);
        when(configurableEnvironmentMock.getProperty(DEFINITION_PROPERTY)).thenReturn("property");

        Set<Class<?>> expectedRegisteredAppClasses = new HashSet<>();
        expectedRegisteredAppClasses.add(Application.class);

        test(configurableEnvironmentMock, expectedRegisteredAppClasses);
    }

    private void test(ConfigurableEnvironment envMock, Set<Class<?>> expectedRegisteredAppClasses) {
        ConfigurableListableBeanFactory beanFactory = prepareTest(envMock);
        performTest(envMock, beanFactory, expectedRegisteredAppClasses);
    }

    @SuppressWarnings("unchecked")
    private ConfigurableListableBeanFactory prepareTest(ConfigurableEnvironment envMock) {
        ConfigurableListableBeanFactory beanFactory = mock(
                ConfigurableListableBeanFactory.class,
                withSettings().extraInterfaces(BeanDefinitionRegistry.class)
        );

        when(beanFactory.getBean(ConfigurableEnvironment.class)).thenReturn(envMock);
        when(beanFactory.getBeanNamesForAnnotation(Path.class)).thenReturn(new String[]{"testResource1", "testResource2"});
        when(beanFactory.getType("testResource1")).thenReturn((Class) TestResource1.class);
        when(beanFactory.getType("testResource2")).thenReturn((Class) TestResource2.class);

        String definition = envMock.getProperty(DEFINITION_PROPERTY);

        if((definition != null && definition.equals("beans"))) {
            // Although TestApplication1 and TestApplication4 are not really Spring beans, here we are simulating
            // they are to see how the JAX-RS Application registration behaves
            Map<String,Application> applicationsMap = new HashMap<String, Application>();
            applicationsMap.put("testApplication1", new TestApplication1());
            applicationsMap.put("testApplication4", new TestApplication4());
            when(beanFactory.getBeansOfType(Application.class, true, false)).thenReturn(applicationsMap);
        }

        return beanFactory;
    }

    private void performTest(ConfigurableEnvironment envMock, ConfigurableListableBeanFactory beanFactory, Set<Class<?>> expectedRegisteredAppClasses) {
        String definition = envMock.getProperty(DEFINITION_PROPERTY);
        boolean findSpringBeans = (definition == null || definition.equals("auto") || definition.equals("beans"));
        boolean getAppsProperty = (definition == null || definition.equals("auto") || definition.equals("property"));

        ResteasyEmbeddedServletInitializer resteasyEmbeddedServletInitializer = new ResteasyEmbeddedServletInitializer();
        resteasyEmbeddedServletInitializer.postProcessBeanFactory(beanFactory);

        verify(beanFactory, VerificationModeFactory.times(getAppsProperty ? 2 : 1)).getBean(ConfigurableEnvironment.class);
        verify(beanFactory, VerificationModeFactory.times(findSpringBeans ? 1 : 0)).getBeansOfType(Application.class, true, false);
        verify(beanFactory, VerificationModeFactory.times(1)).getBeanNamesForAnnotation(Path.class);
        verify(beanFactory, VerificationModeFactory.times(1)).getBeanNamesForAnnotation(Provider.class);
        verify(beanFactory, VerificationModeFactory.times(2)).getType(anyString());

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

        Set<Class<?>> expectedNotRegisteredAppClassess = new HashSet<>(allPossibleAppClasses);
        for(Class<?> applicationClass : expectedRegisteredAppClasses) {
            verify(registry, VerificationModeFactory.times(1)).registerBeanDefinition(eq(applicationClass.getName()), any(GenericBeanDefinition.class));
            expectedNotRegisteredAppClassess.remove(applicationClass);
        }
        for(Class<?> applicationClass : expectedNotRegisteredAppClassess) {
            verify(registry, VerificationModeFactory.times(0)).registerBeanDefinition(eq(applicationClass.getName()), any(GenericBeanDefinition.class));
        }
    }

}
