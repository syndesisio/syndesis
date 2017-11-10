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
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings({"PMD.AvoidSynchronizedAtMethodLevel", "PMD.AvoidCatchingGenericException", "PMD.ExcessiveImports"})
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
    SyndesisExtensionActionProcessor.ANNOTATION_NAME
})
public class SyndesisExtensionActionProcessor extends AbstractProcessor {
    public static final String ANNOTATION_NAME = "io.syndesis.integration.runtime.api.SyndesisExtensionAction";
    public static final String STEP_EXTENSION_NAME = "io.syndesis.integration.runtime.api.SyndesisStepExtension";

    public TypeMirror steExtensionType;
    public Class<? extends Annotation> annotationClass ;

    @Override
    public synchronized void init(ProcessingEnvironment env){
        this.processingEnv = env;
        this.steExtensionType = processingEnv.getElementUtils().getTypeElement(STEP_EXTENSION_NAME).asType();

        try {
            annotationClass = (Class<? extends Annotation>) Class.forName(ANNOTATION_NAME);
        } catch (ClassNotFoundException e) {
            error("Unable to find Annotation " +  ANNOTATION_NAME + " on Classpath");
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        // a lot of noisy logic to prevent this method to ever fail, since it's required by the compiler implicit contract
        if(annotationClass == null){
            return false;
        }
        for (Element annotatedElement : env.getElementsAnnotatedWith(annotationClass)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                return true; // Exit processing
            }
            try {
                TypeElement typedElement = (TypeElement) annotatedElement;
                Properties props = gatherProperties(typedElement);
                augmentProperties(typedElement, props);
                persistToFile(typedElement, props);
            } catch (IOException|InvocationTargetException|IllegalAccessException e){
                return false;
            }
        }
        return false;
    }

    /**
     * Explicitly add properties that elude reflection implicit strategy
     * @param typedElement
     * @param props
     */
    protected void augmentProperties(TypeElement typedElement, Properties props) {
        if (processingEnv.getTypeUtils().isAssignable(typedElement.asType(), steExtensionType)) {
            props.put("kind", "STEP");
            props.put("entrypoint", typedElement.getQualifiedName().toString());
        } else {
            props.put("kind", "BEAN");
            props.put("entrypoint", typedElement.getQualifiedName().toString());
        }
    }

    protected Properties gatherProperties(TypeElement classElement) throws InvocationTargetException, IllegalAccessException {
        Properties prop = new Properties();
        Annotation annotation = classElement.getAnnotation(annotationClass);
        Method[] methods = annotationClass.getDeclaredMethods();
        for (Method m : methods) {
            writeIfNotEmpty(prop, m.getName(), m.invoke(annotation));
        }
        return prop;
    }

    protected void persistToFile(TypeElement classElement, Properties props) throws IOException {
        File file = obtainResourceFile(classElement);
        try(Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            props.store(writer, "Generated by Syndesis Annotation Processor");
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
    protected File obtainResourceFile(TypeElement classElement) throws IOException {
        File result = null;
        Filer filer = processingEnv.getFiler();

        final String javaTypeName = canonicalClassName(classElement.getQualifiedName().toString());
        String packageName = javaTypeName.substring(0, javaTypeName.lastIndexOf('.'));
        String fileName = classElement.getSimpleName().toString() + ".properties";

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
