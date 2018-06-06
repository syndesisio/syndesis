package com.paypal.springboot.resteasy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.servlet.ResteasyServletInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * This is a Spring version of {@link ResteasyServletInitializer}.
 * It does not register the servlets though, that is done by {@link ResteasyApplicationBuilder}
 * It only finds the JAX-RS Application classes (by scanning the classpath), and
 * the JAX-RS Path and Provider annotated Spring beans, and then register the
 * Spring bean definitions that represent each servlet registration.
 *
 * @author Fabio Carvalho (facarvalho@paypal.com or fabiocarvalho777@gmail.com)
 */
public class ResteasyEmbeddedServletInitializer implements BeanFactoryPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResteasyEmbeddedServletInitializer.class);

    private static final String JAXRS_APP_CLASSES_DEFINITION_PROPERTY = "resteasy.jaxrs.app.registration";
    private static final String JAXRS_APP_CLASSES_PROPERTY = "resteasy.jaxrs.app.classes";

    // This is how JAXRS_APP_CLASSES_PROPERTY was named originally. It conflicted with "resteasy.jaxrs.app.registration"
    // in case of YAML files, since registration was a child of app from an YAML perspective, which is not allowed.
    // Because of that its name was changed (the ".classes" suffix was added).
    // This legacy property has not been removed though, to keep backward compatibility, but it is marked as deprecated. It will be
    // available only for .properties files, but not for YAML files. It should be finally removed in a future major release.
    private static final String JAXRS_APP_CLASSES_PROPERTY_LEGACY = "resteasy.jaxrs.app";

    private final Set<Class<? extends Application>> applications = new HashSet<>();
    private final Set<Class<?>> allResources = new HashSet<>();
    private final Set<Class<?>> providers = new HashSet<>();

    private enum JaxrsAppClassesRegistration {
        BEANS, PROPERTY, SCANNING, AUTO
    }

    /*
     * Find the JAX-RS application classes.
     * This is done by one of these three options in this order:
     *
     * 1- By having them defined as Spring beans
     * 2- By setting property {@code resteasy.jaxrs.app.classes} via Spring Boot application properties file.
     *    This property should contain a comma separated list of JAX-RS sub-classes
     * 3- Via classpath scanning (looking for javax.ws.rs.core.Application sub-classes)
     *
     * First try to find JAX-RS Application sub-classes defined as Spring beans. If that is existent,
     * the search stops, and those are the only JAX-RS applications to be registered.
     * If no JAX-RS application Spring beans are found, then see if Spring Boot property {@code resteasy.jaxrs.app.classes}
     * has been set. If it has, the search stops, and those are the only JAX-RS applications to be registered.
     * If not, then scan the classpath searching for JAX-RS applications.
     *
     * There is a way though to force one of the options above, which is by setting property
     * {@code resteasy.jaxrs.app.registration} via Spring Boot application properties file. The possible valid
     * values are {@code beans}, {@code property}, {@code scanning} or {@code auto}. If this property is not
     * present, the default value is {@code auto}, which means every approach will be tried in the order and way
     * explained earlier.
     *
     * @param beanFactory
     */
    private void findJaxrsApplications(ConfigurableListableBeanFactory beanFactory) {
        LOGGER.info("Finding JAX-RS Application classes");

        JaxrsAppClassesRegistration registration = getJaxrsAppClassesRegistration(beanFactory);

        switch (registration) {
            case AUTO:
                findJaxrsApplicationBeans(beanFactory);
                if(applications.isEmpty()) {
                    findJaxrsApplicationProperty(beanFactory);
                }
                if(applications.isEmpty()) {
                    findJaxrsApplicationScanning(beanFactory);
                }
                break;
            case BEANS:
                findJaxrsApplicationBeans(beanFactory);
                break;
            case PROPERTY:
                findJaxrsApplicationProperty(beanFactory);
                break;
            case SCANNING:
                // TODO
                // Remove this warning after implementing
                // https://github.com/paypal/resteasy-spring-boot/issues/69
                LOGGER.warn("\n-------------\nStarting on version 3.0.0, the behavior of the `scanning` JAX-RS Application subclass registration method will change, being more restrictive.\nInstead of scanning the whole classpath, it will scan only packages registered to be scanned by Spring framework (regardless of the JAX-RS Application subclass being a Spring bean or not). The reason is to improve application startup performance.\nHaving said that, it is recommended that every application use any method, other than `scanning`. Or, if using `scanning`, make sure your JAX-RS Application subclass is under a package to be scanned by Spring framework. If not, starting on version 3.0.0,it won't be found.\n-------------");
                findJaxrsApplicationScanning(beanFactory);
                break;
            default:
                LOGGER.error("JAX-RS application registration method (%s) not known, no application will be registered", registration.name());
                break;
        }

        for (Class<? extends Application> appClass : applications) {
            LOGGER.info("JAX-RS Application class found: {}", appClass.getName());
        }
    }

    @SuppressWarnings("PMD.UseLocaleWithCaseConversions")
    private JaxrsAppClassesRegistration getJaxrsAppClassesRegistration(ConfigurableListableBeanFactory beanFactory) {
        ConfigurableEnvironment configurableEnvironment = beanFactory.getBean(ConfigurableEnvironment.class);
        String jaxrsAppClassesRegistration = configurableEnvironment.getProperty(JAXRS_APP_CLASSES_DEFINITION_PROPERTY);
        JaxrsAppClassesRegistration registration = JaxrsAppClassesRegistration.AUTO;

        if (jaxrsAppClassesRegistration == null) {
            LOGGER.info("Property {} has not been set, JAX-RS Application classes registration is being set to AUTO", JAXRS_APP_CLASSES_DEFINITION_PROPERTY);
        } else {
            LOGGER.info("Property {} has been set to {}", JAXRS_APP_CLASSES_DEFINITION_PROPERTY, jaxrsAppClassesRegistration);
            try {
                registration = JaxrsAppClassesRegistration.valueOf(jaxrsAppClassesRegistration.toUpperCase());
            } catch(IllegalArgumentException ex) {
                String errorMesage = String.format("Property %s has not been properly set, value %s is invalid. JAX-RS Application classes registration is being set to AUTO.", JAXRS_APP_CLASSES_DEFINITION_PROPERTY, jaxrsAppClassesRegistration);
                LOGGER.error(errorMesage);
                throw new IllegalArgumentException(errorMesage, ex);
            }
        }

        return registration;
    }

    /*
     * Find JAX-RS application classes by searching for their related
     * Spring beans
     *
     * @param beanFactory
     */
    private void findJaxrsApplicationBeans(ConfigurableListableBeanFactory beanFactory) {
        LOGGER.info("Searching for JAX-RS Application Spring beans");

        Map<String, Application> applicationBeans = beanFactory.getBeansOfType(Application.class, true, false);
        if (applicationBeans == null || applicationBeans.isEmpty()) {
            LOGGER.info("No JAX-RS Application Spring beans found");
            return;
        }

        for (Application application : applicationBeans.values()) {
            applications.add(application.getClass());
        }
    }

    /*
     * Find JAX-RS application classes via property {@code resteasy.jaxrs.app.classes}
     */
    @SuppressWarnings("unchecked")
    private void findJaxrsApplicationProperty(ConfigurableListableBeanFactory beanFactory) {
        ConfigurableEnvironment configurableEnvironment = beanFactory.getBean(ConfigurableEnvironment.class);
        String jaxrsAppsProperty = configurableEnvironment.getProperty(JAXRS_APP_CLASSES_PROPERTY);
        if (jaxrsAppsProperty == null) {
            jaxrsAppsProperty = configurableEnvironment.getProperty(JAXRS_APP_CLASSES_PROPERTY_LEGACY);
            if(jaxrsAppsProperty == null) {
                LOGGER.info("No JAX-RS Application set via property {}", JAXRS_APP_CLASSES_PROPERTY);
                return;
            }
            LOGGER.warn("Property {} has been set. Notice that this property has been deprecated and will be removed soon. Please replace it by property {}", JAXRS_APP_CLASSES_PROPERTY_LEGACY, JAXRS_APP_CLASSES_PROPERTY);
        } else {
            LOGGER.info("Property {} has been set to {}", JAXRS_APP_CLASSES_PROPERTY, jaxrsAppsProperty);
        }

        String[] jaxrsClassNames = jaxrsAppsProperty.split(",", -1);

        for(String jaxrsClassName : jaxrsClassNames) {
            Class<? extends Application> jaxrsClass = null;
            try {
                jaxrsClass = (Class<? extends Application>) Class.forName(jaxrsClassName.trim());
            } catch (ClassNotFoundException e) {
                String exceptionMessage = String.format("JAX-RS Application class %s has not been found", jaxrsClassName.trim());
                LOGGER.error(exceptionMessage, e);
                throw new BeansException(exceptionMessage, e){};
            }
            applications.add(jaxrsClass);
        }
    }

    /*
     * Find JAX-RS application classes by scanning the classpath under
     * packages already marked to be scanned by Spring framework
     */
    private void findJaxrsApplicationScanning(BeanFactory beanFactory) {
        List<String> packagesToBeScanned = getSpringApplicationPackages(beanFactory);

        Set<Class<? extends Application>> applications = JaxrsApplicationScanner.getApplications(packagesToBeScanned);
        if(applications == null || applications.isEmpty()) {
            return;
        }
        this.applications.addAll(applications);
    }

    /*
     * Return the name of the packages to be scanned by Spring framework
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private List<String> getSpringApplicationPackages(BeanFactory beanFactory) {
        // TODO
        // See https://github.com/paypal/resteasy-spring-boot/issues/69

        List<String> packages = new ArrayList<>();
        packages.add("");
        return packages;
    }

    /*
     * Search for JAX-RS resource and provider Spring beans,
     * which are the ones whose classes are annotated with
     * {@link Path} or {@link Provider} respectively
     *
     * @param beanFactory
     */
    private void findJaxrsResourcesAndProviderClasses(ConfigurableListableBeanFactory beanFactory) {
        LOGGER.debug("Finding JAX-RS resources and providers Spring bean classes");

        String[] resourceBeans = beanFactory.getBeanNamesForAnnotation(Path.class);
        String[] providerBeans = beanFactory.getBeanNamesForAnnotation(Provider.class);

        if(resourceBeans != null) {
            for(String resourceBean : resourceBeans) {
                allResources.add(beanFactory.getType(resourceBean));
            }
        }

        if(providerBeans != null) {
            for(String providerBean : providerBeans) {
                providers.add(beanFactory.getType(providerBean));
            }
        }

        if(LOGGER.isDebugEnabled()) {
            for (Object resourceClass : allResources.toArray()) {
                LOGGER.debug("JAX-RS resource class found: {}", ((Class) resourceClass).getName());
            }
        }
        if(LOGGER.isDebugEnabled()) {
            for (Object providerClass: providers.toArray()) {
                LOGGER.debug("JAX-RS provider class found: {}", ((Class) providerClass).getName());
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        LOGGER.debug("Post process bean factory has been called");

        findJaxrsApplications(beanFactory);

        // This is done by finding their related Spring beans
        findJaxrsResourcesAndProviderClasses(beanFactory);

        if (allResources.isEmpty()) {
            LOGGER.warn("No JAX-RS resource Spring beans have been found");
        }
        if (applications.isEmpty()) {
            LOGGER.info("No JAX-RS Application classes have been found. A default, one mapped to '/', will be registered.");
            registerDefaultJaxrsApp(beanFactory);
            return;
        }

        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

        for (Class<? extends Application> applicationClass : applications) {
            ApplicationPath path = AnnotationUtils.findAnnotation(applicationClass, ApplicationPath.class);
            if (path == null) {
                LOGGER.warn("JAX-RS Application class {} has no ApplicationPath annotation, so it will not be registered", applicationClass.getName());
                continue;
            }

            LOGGER.debug("registering JAX-RS application class {}", applicationClass.getName());

            GenericBeanDefinition applicationServletBean = createApplicationServlet(applicationClass, path.value());
            registry.registerBeanDefinition(applicationClass.getName(), applicationServletBean);
        }

    }

    /**
     * Register a default JAX-RS application, in case no other is present in the application.
     * Read section 2.3.2 in JAX-RS 2.0 specification.
     *
     * @param beanFactory
     */
    private void registerDefaultJaxrsApp(ConfigurableListableBeanFactory beanFactory) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        GenericBeanDefinition applicationServletBean = createApplicationServlet(Application.class, "/");
        registry.registerBeanDefinition(Application.class.getName(), applicationServletBean);
    }

    /**
     * Creates a Servlet bean definition for the given JAX-RS application
     *
     * @param applicationClass
     * @param path
     * @return a Servlet bean definition for the given JAX-RS application
     */
    private GenericBeanDefinition createApplicationServlet(Class<? extends Application> applicationClass, String path) {
        GenericBeanDefinition applicationServletBean = new GenericBeanDefinition();
        applicationServletBean.setFactoryBeanName(ResteasyApplicationBuilder.BEAN_NAME);
        applicationServletBean.setFactoryMethodName("build");

        Set<Class<?>> resources = allResources;

        ConstructorArgumentValues values = new ConstructorArgumentValues();
        values.addIndexedArgumentValue(0, applicationClass.getName());
        values.addIndexedArgumentValue(1, path);
        values.addIndexedArgumentValue(2, resources);
        values.addIndexedArgumentValue(3, providers);
        applicationServletBean.setConstructorArgumentValues(values);

        applicationServletBean.setAutowireCandidate(false);
        applicationServletBean.setScope("singleton");

        return applicationServletBean;
    }

}
