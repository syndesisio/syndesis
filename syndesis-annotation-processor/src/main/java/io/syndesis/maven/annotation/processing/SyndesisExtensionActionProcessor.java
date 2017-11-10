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
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
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
import io.syndesis.integration.runtime.api.SyndesisExtensionAction;
import io.syndesis.integration.runtime.api.SyndesisStepExtension;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.springframework.context.annotation.Bean;

@SuppressWarnings({"PMD.AvoidSynchronizedAtMethodLevel", "PMD.AvoidCatchingGenericException", "PMD.ExcessiveImports"})
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
    SyndesisExtensionActionProcessor.ANNOTATION_NAME
})
public class SyndesisExtensionActionProcessor extends AbstractProcessor {
    public static final String ANNOTATION_NAME = "io.syndesis.integration.runtime.api.SyndesisExtensionAction";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (Element annotatedElement : env.getElementsAnnotatedWith(SyndesisExtensionAction.class)) {
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
        Elements elements = processingEnv.getElementUtils();
        TypeMirror extensionType = elements.getTypeElement(SyndesisStepExtension.class.getName()).asType();

        if (processingEnv.getTypeUtils().isAssignable(element.asType(), extensionType)) {
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
    protected void augmentProperties(ExecutableElement element, Properties props) {
        SyndesisExtensionAction action = element.getAnnotation(SyndesisExtensionAction.class);
        Elements elements = processingEnv.getElementUtils();
        Types types = processingEnv.getTypeUtils();

        TypeElement typedElement = (TypeElement) element.getEnclosingElement();
        TypeMirror returnType = element.getReturnType();
        TypeMirror routeBuilderType = elements.getTypeElement(RouteBuilder.class.getName()).asType();
        TypeMirror routeDefinitionType = elements.getTypeElement(RouteDefinition.class.getName()).asType();

        if (element.getAnnotation(Bean.class) != null && (types.isAssignable(returnType, routeBuilderType) || types.isAssignable(returnType, routeDefinitionType))) {
            String entrypoint = action.entrypoint();
            if (entrypoint != null && !entrypoint.isEmpty()) {
                props.put("kind", "ROUTE");
                props.put("entrypoint", entrypoint);
            } else {
                warning("Action with id '" + action.id() + "' must define an entrypoint");
            }
        } else if (element.getAnnotation(Bean.class) == null && !types.isAssignable(returnType, routeBuilderType) && !types.isAssignable(returnType, routeDefinitionType)) {
            props.put("kind", "BEAN");
            props.put("entrypoint", typedElement.getQualifiedName().toString() + "::" + element.getSimpleName());
        }
    }

    protected Properties gatherProperties(Element element) throws InvocationTargetException, IllegalAccessException {
        Properties prop = new Properties();
        Annotation annotation = element.getAnnotation(SyndesisExtensionAction.class);
        Method[] methods = SyndesisExtensionAction.class.getDeclaredMethods();
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
    public static String canonicalClassName(String className) {
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
            .append(element.getAnnotation(SyndesisExtensionAction.class).id())
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

    public void info(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    public void warning(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
    }

    public void error(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

}
