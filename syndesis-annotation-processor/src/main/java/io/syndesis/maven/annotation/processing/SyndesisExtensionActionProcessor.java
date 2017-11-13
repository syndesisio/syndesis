/*
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
package io.syndesis.maven.annotation.processing;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings({"PMD.AvoidSynchronizedAtMethodLevel", "PMD.AvoidCatchingGenericException", "PMD.ExcessiveImports"})
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
    SyndesisExtensionActionProcessor.SYNDESIS_ANNOTATION_CLASS_NAME
})
public class SyndesisExtensionActionProcessor extends AbstractProcessor {
    public static final String SYNDESIS_ANNOTATION_CLASS_NAME = "io.syndesis.integration.runtime.api.SyndesisExtensionAction";
    public static final String SYNDESIS_STEP_CLASS_NAME = "io.syndesis.integration.runtime.api.SyndesisStepExtension";
    public static final String BEAN_ANNOTATION_CLASS_NAME = "org.springframework.context.annotation.Bean";
    public static final String ROUTE_BUILDER_CLASS_NAME = "org.apache.camel.builder.RouteBuilder";
    public static final String ROUTE_DEFINITION_CLASS_NAME = "org.apache.camel.model.RouteDefinition";

    private Class<? extends Annotation> annotationClass ;
    private Class<? extends Annotation> beanAnnotationClass;
    private Class<?> stepClass;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        annotationClass = (Class<? extends Annotation>)mandatoryFindClass(SYNDESIS_ANNOTATION_CLASS_NAME);
        stepClass = findClass(SYNDESIS_STEP_CLASS_NAME);
        beanAnnotationClass = (Class<? extends Annotation>)findClass(BEAN_ANNOTATION_CLASS_NAME);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        // a lot of noisy logic to prevent this method to ever fail, since it's required by the compiler implicit contract
        if(annotationClass == null){
            return false;
        }

        for (Element annotatedElement : env.getElementsAnnotatedWith(annotationClass)) {
            if (annotatedElement.getKind() == ElementKind.CLASS) {
                try {
                    Properties props = gatherProperties(annotatedElement);
                    augmentProperties((TypeElement) annotatedElement, props);
                    persistToFile(annotatedElement, props);
                } catch (IOException|InvocationTargetException|IllegalAccessException e){
                    return false;
                }
            } else if (annotatedElement.getKind() == ElementKind.METHOD) {
                try {
                    Properties props = gatherProperties(annotatedElement);
                    augmentProperties((ExecutableElement) annotatedElement, props);
                    persistToFile(annotatedElement, props);
                } catch (IOException|InvocationTargetException|IllegalAccessException e){
                    return false;
                }
            } else {
                return true; // Exit processing
            }
        }
        return false;
    }

    /**
     * Explicitly add properties that elude reflection implicit strategy
     * @param element
     * @param props
     */
    protected void augmentProperties(TypeElement element, Properties props) {
        final Elements elements = processingEnv.getElementUtils();
        final TypeElement extensionTypeElement = elements.getTypeElement(stepClass.getName());

        if (extensionTypeElement != null && processingEnv.getTypeUtils().isAssignable(element.asType(), extensionTypeElement.asType())) {
            props.put("kind", "STEP");
            props.put("entrypoint", element.getQualifiedName().toString());
        } else {
            props.put("kind", "BEAN");
            props.put("entrypoint", element.getQualifiedName().toString());
        }
    }

    /**
     * Explicitly add properties that elude reflection implicit strategy
     * @param element
     * @param props
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    protected void augmentProperties(ExecutableElement element, Properties props) {
        final Elements elements = processingEnv.getElementUtils();
        final Types types = processingEnv.getTypeUtils();
        final TypeElement typedElement = (TypeElement) element.getEnclosingElement();
        final TypeMirror returnType = element.getReturnType();
        final TypeElement routeBuilderElement = elements.getTypeElement(ROUTE_BUILDER_CLASS_NAME);
        final TypeElement routeDefinitionElement = elements.getTypeElement(ROUTE_DEFINITION_CLASS_NAME);

        if (beanAnnotationClass != null && element.getAnnotation(beanAnnotationClass) != null) {
            if (types.isAssignable(returnType, routeBuilderElement.asType()) || types.isAssignable(returnType, routeDefinitionElement.asType())) {
                props.put("kind", "ROUTE");
            }
        } else {
            if (routeBuilderElement == null || routeDefinitionElement == null) {
                props.put("kind", "BEAN");
                props.put("entrypoint", typedElement.getQualifiedName().toString() + "::" + element.getSimpleName());
            } else if (!types.isAssignable(returnType, routeBuilderElement.asType()) && !types.isAssignable(returnType, routeDefinitionElement.asType())) {
                props.put("kind", "BEAN");
                props.put("entrypoint", typedElement.getQualifiedName().toString() + "::" + element.getSimpleName());
            }
        }
    }

    protected Properties gatherProperties(Element element) throws InvocationTargetException, IllegalAccessException {
        Properties prop = new Properties();
        Annotation annotation = element.getAnnotation(annotationClass);
        Method[] methods = annotationClass.getDeclaredMethods();
        for (Method m : methods) {
            writeIfNotEmpty(prop, m.getName(), m.invoke(annotation));
        }
        return prop;
    }

    protected void persistToFile(Element element, Properties props) throws IOException {
        File file = obtainResourceFile(element);
        if (file != null) {
            try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                props.store(writer, "Generated by Syndesis Annotation Processor");
            }
        }
    }


    protected void writeIfNotEmpty(Properties prop, String key, Object value) {
        if(value != null && !"".equals(value.toString().trim())){
            if(value instanceof String[]){
                String[] arr = (String[])value;
                if(arr.length > 0){
                    prop.put(key, String.join(",", arr));
                }
            } else {
                prop.put(key, value);
            }
        }
    }

    /**
     * Returns the canonical class name by removing any generic type information.
     */
    protected static String canonicalClassName(String className) {
        // remove generics
        int pos = className.indexOf('<');
        if (pos != -1) {
            return className.substring(0, pos);
        } else {
            return className;
        }
    }


    /**
     * Helper method to produce class output text file using the given handler
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    protected File obtainResourceFile(Element element) throws IOException {
        TypeElement classElement;
        if (element instanceof TypeElement) {
            classElement = (TypeElement)element;
        } else if (element instanceof ExecutableElement) {
            classElement = (TypeElement)element.getEnclosingElement();
        } else {
            warning("Unsupported element kind: " + element.getKind());
            return null;
        }

        final String javaTypeName = canonicalClassName(classElement.getQualifiedName().toString());
        final String packageName = javaTypeName.substring(0, javaTypeName.lastIndexOf('.'));

        final String fileName = new StringBuilder()
            .append(classElement.getSimpleName().toString())
            .append('-')
            .append(UUID.randomUUID().toString())
            .append(".properties")
            .toString();

        File result = null;
        Filer filer = processingEnv.getFiler();
        FileObject resource;
        try {
            resource = filer.getResource(StandardLocation.SOURCE_OUTPUT, packageName, fileName);
        } catch (Exception e) {
            resource = filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, fileName);
        }
        URI uri = resource.toUri();
        if (uri != null) {
            try {
                result = new File(uri.getPath());
            } catch (Exception e) {
                warning("Cannot convert output directory resource URI to a file " + e);
            }
        }
        if (result == null) {
            warning("No class output directory could be found!");
        } else {
            result.getParentFile().mkdirs();
        }
        return result;
    }

    protected void info(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    protected void warning(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
    }

    protected void error(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

    protected Class<?> mandatoryFindClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            error("Unable to find Class " +  name + " on Classpath");
        }

        return null;
    }

    protected Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            warning("Unable to find Class " +  name + " on Classpath");
        }

        return null;
    }
}
