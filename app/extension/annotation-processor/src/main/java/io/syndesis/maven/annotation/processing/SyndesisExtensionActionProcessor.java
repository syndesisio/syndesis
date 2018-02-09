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
import java.util.Locale;
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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("PMD")
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
    SyndesisExtensionActionProcessor.SYNDESIS_ANNOTATION_CLASS_NAME
})
public class SyndesisExtensionActionProcessor extends AbstractProcessor {
    public static final String SYNDESIS_ANNOTATION_CLASS_NAME = "io.syndesis.extension.api.SyndesisExtensionAction";
    public static final String SYNDESIS_PROPERTY_ANNOTATION_CLASS_NAME = "io.syndesis.extension.api.SyndesisActionProperty";
    public static final String SYNDESIS_PROPERTY_ENUM_ANNOTATION_CLASS_NAME = "io.syndesis.extension.api.SyndesisActionProperty$PropertyEnum";
    public static final String SYNDESIS_STEP_CLASS_NAME = "io.syndesis.extension.api.SyndesisStepExtension";
    public static final String CAMEL_HANDLER_ANNOTATION_CLASS_NAME_ = "org.apache.camel.Handler";
    public static final String BEAN_ANNOTATION_CLASS_NAME = "org.springframework.context.annotation.Bean";

    private Class<? extends Annotation> annotationClass;
    private Class<? extends Annotation> propertyAnnotationClass;
    private Class<? extends Annotation> propertyEnumAnnotationClass;
    private Class<? extends Annotation> beanAnnotationClass;
    private Class<? extends Annotation> handlerAnnotationClass;
    private Class<?> stepClass;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        annotationClass = mandatoryFindClass(SYNDESIS_ANNOTATION_CLASS_NAME);
        propertyAnnotationClass = mandatoryFindClass(SYNDESIS_PROPERTY_ANNOTATION_CLASS_NAME);
        propertyEnumAnnotationClass = mandatoryFindClass(SYNDESIS_PROPERTY_ENUM_ANNOTATION_CLASS_NAME);
        stepClass = findClass(SYNDESIS_STEP_CLASS_NAME);
        beanAnnotationClass = findClass(BEAN_ANNOTATION_CLASS_NAME);
        handlerAnnotationClass = mandatoryFindClass(CAMEL_HANDLER_ANNOTATION_CLASS_NAME_);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        // a lot of noisy logic to prevent this method to ever fail, since it's required by the compiler implicit contract
        if(annotationClass == null){
            return false;
        }

        // If true the annotation types are claimed and subsequent processors
        // will not be asked to process them.
        boolean claimed = false;

        for (Element annotatedElement : env.getElementsAnnotatedWith(annotationClass)) {
            if (annotatedElement.getKind() == ElementKind.CLASS) {
                try {
                    Properties props = gatherProperties(annotatedElement);
                    claimed = augmentProperties((TypeElement) annotatedElement, props);
                    addActionProperties(annotatedElement, props);
                    persistToFile(annotatedElement, props);
                } catch (IOException|InvocationTargetException|IllegalAccessException|NoSuchMethodException e){
                    claimed = false;
                }
            } else if (annotatedElement.getKind() == ElementKind.METHOD) {
                try {
                    Properties props = gatherProperties(annotatedElement);
                    augmentProperties((ExecutableElement) annotatedElement, props);
                    addActionProperties(annotatedElement, props);
                    persistToFile(annotatedElement, props);
                } catch (IOException|InvocationTargetException|IllegalAccessException|NoSuchMethodException e){
                    claimed = false;
                }
            } else {
                claimed = true;
            }
        }

        return claimed;
    }

    /**
     * Explicitly add properties that elude reflection implicit strategy
     * @param element
     * @param props
     */
    protected boolean augmentProperties(TypeElement element, Properties props) throws InvocationTargetException, IllegalAccessException {
        final Elements elements = processingEnv.getElementUtils();
        final TypeElement extensionTypeElement = elements.getTypeElement(stepClass.getName());

        if (extensionTypeElement != null && processingEnv.getTypeUtils().isAssignable(element.asType(), extensionTypeElement.asType())) {
            props.put("kind", "STEP");
            props.put("entrypoint", element.getQualifiedName().toString());

            // Let's search for fields annotated with SyndesisActionProperty
            for (Element field: element.getEnclosedElements()) {
                if (field.getKind() == ElementKind.FIELD) {
                    addActionProperties(field, props);
                }
            }

            return true;
        } else {
            props.put("kind", "BEAN");
            props.put("entrypoint", element.getQualifiedName().toString());

            for (Element method: element.getEnclosedElements()) {
                if (method.getAnnotation(handlerAnnotationClass) != null) {
                    // Process method
                    augmentProperties((ExecutableElement)method, props);
                    addActionProperties(method, props);

                    // Found a method annotated with Handler, let's search for
                    // fields annotated with SyndesisActionProperty
                    for (Element field: element.getEnclosedElements()) {
                        if (field.getKind() == ElementKind.FIELD) {
                            addActionProperties(field, props);
                        }
                    }

                    // No need to go ahead as this is the method that implements
                    // the action
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Explicitly add properties that elude reflection implicit strategy
     * @param element
     * @param props
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    protected void augmentProperties(ExecutableElement element, Properties props) {
        final TypeElement typedElement = (TypeElement) element.getEnclosingElement();

        if (beanAnnotationClass != null && element.getAnnotation(beanAnnotationClass) != null) {
            props.put("kind", "ENDPOINT");
        } else {
            props.put("kind", "BEAN");
            props.put("entrypoint", typedElement.getQualifiedName().toString() + "::" + element.getSimpleName());
        }
    }

    /**
     * Add action properties to the global properties.
     * @param element
     * @param props
     */
    protected void addActionProperties(Element element, Properties props) throws InvocationTargetException, IllegalAccessException {
        int index = 0;

        for (String key : props.stringPropertyNames()) {
            String prefix = "property[" + index + "].";

            if (key.startsWith(prefix)) {
                index++;
            }
        }

        Annotation[] annotations = element.getAnnotationsByType(propertyAnnotationClass);
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            Properties propData = gatherProperties(annotation, propertyAnnotationClass);

            for (String key : propData.stringPropertyNames()) {
                writeIfNotEmpty(props, "property[" + (index + i) + "]." + key, propData.getProperty(key));
            }

            if (element.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement)element;

                TypeMirror typeMirror = field.asType();
                TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(typeMirror.toString());
                String javaType = typeMirror.toString();
                String type = propData.getProperty("type");

                if (typeElement != null && typeElement.getKind() == ElementKind.ENUM) {
                    int enumIndex = 0;
                    for (Element enumElement: typeElement.getEnclosedElements()) {
                        if (enumElement.getKind() == ElementKind.ENUM_CONSTANT) {
                            writeIfNotEmpty(props, "property[" + (index + i) + "].enums[" + enumIndex + "].label" , enumElement.toString());
                            writeIfNotEmpty(props, "property[" + (index + i) + "].enums[" + enumIndex + "].value" , enumElement.toString());

                            enumIndex++;
                        }
                    }

                    javaType = String.class.getName();
                    type = String.class.getName();
                }

                if (type == null || "".equals(type.trim())){
                    if (String.class.getName().equals(type)) {
                        type = "string";
                    } else if (Boolean.class.getName().equals(type)) {
                        type = "boolean";
                    } else if (Integer.class.getName().equals(type)) {
                        type = "int";
                    } else if (Float.class.getName().equals(type)) {
                        type = "float";
                    } else if (Double.class.getName().equals(type)) {
                        type = "double";
                    }
                }

                writeIfNotEmpty(props, "property[" + (index + i) + "].javaType", javaType);
                writeIfNotEmpty(props, "property[" + (index + i) + "].type", type);
            }
        }
    }

    protected Properties gatherProperties(Element element) throws InvocationTargetException, IllegalAccessException {
        Annotation annotation = element.getAnnotation(annotationClass);
        return gatherProperties(annotation, annotationClass);
    }

    protected Properties gatherProperties(Annotation annotation, Class<? extends Annotation> clazz) throws InvocationTargetException, IllegalAccessException  {
        Properties prop = new Properties();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method m : methods) {
            writeIfNotEmpty(prop, m.getName(), m.invoke(annotation));
        }
        return prop;
    }

    protected void persistToFile(Element element, Properties props) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        File file = obtainResourceFile(element);
        if (file != null) {
            try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                props.store(writer, "Generated by Syndesis Annotation Processor");
            }
        }
    }


    protected void writeIfNotEmpty(Properties prop, String key, Object value) throws InvocationTargetException, IllegalAccessException {
        if(value != null && !"".equals(value.toString().trim())) {
            if(value instanceof String[]){
                String[] arr = (String[])value;
                if(arr.length > 0){
                    prop.put(key, String.join(",", arr));
                }
            } else if(Object[].class.isInstance(value)) {
                Object[] array = (Object[]) value;
                for (int i=0; i<array.length; i++) {
                    if (propertyEnumAnnotationClass.isInstance(array[i])) {
                        Annotation enumAnn = (Annotation) array[i];
                        Properties props = gatherProperties(enumAnn, propertyEnumAnnotationClass);
                        for (String propName : props.stringPropertyNames()) {
                            prop.put(key + "[" + i + "]." + propName, props.getProperty(propName));
                        }
                    }
                }
            } else {
                prop.put(key, value.toString());
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
    protected File obtainResourceFile(Element element) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
        final Annotation annotation = element.getAnnotation(annotationClass);

        if (annotation == null) {
            error("Annotation SyndesisExtensionAction not found processing element " + element);
        }

        final String actionId = (String)annotationClass.getMethod("id").invoke(annotation);
        final String fileName = new StringBuilder()
            .append(classElement.getSimpleName().toString())
            .append('-')
            .append(Names.sanitize(actionId))
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

    private Class<? extends Annotation> mandatoryFindClass(String name) {
        try {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Class<? extends Annotation> ret = (Class) Class.forName(name);
            return ret;
        } catch (ClassNotFoundException e) {
            error("Unable to find Class " +  name + " on Classpath");
        }

        return null;
    }

    private Class<? extends Annotation> findClass(String name) {
        try {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            Class<? extends Annotation> ret = (Class) Class.forName(name);
            return ret;
        } catch (ClassNotFoundException e) {
            warning("Unable to find Class " +  name + " on Classpath");
        }

        return null;
    }

    // From app/rest/core
    public static final class Names {
        private static final String INVALID_CHARACTER_REGEX = "[^a-zA-Z0-9-]";
        private static final String SPACE = " ";
        private static final String BLANK = "";
        private static final String DASH = "-";

        /**
         * Sanitizes the specified name by applying the following rules:
         * 1. Keep the first 100 characters.
         * 2. Replace spaces with dashes.
         * 3. Remove invalid characters.
         * @param name  The specified name.
         * @return      The sanitized string.
         */
        static String sanitize(String name) {
            return name
                .replaceAll(SPACE, DASH)
                .replaceAll(INVALID_CHARACTER_REGEX, BLANK)
                .toLowerCase(Locale.US)
                .chars()
                .filter(i -> !String.valueOf(i).matches(INVALID_CHARACTER_REGEX))
                .collect(StringBuilder::new,
                    (b, chr) -> {
                        int lastChar = b.length() > 0 ? b.charAt(b.length() - 1) : -1;

                        if (lastChar != '-' || chr != '-') {
                            b.appendCodePoint(chr);
                        }
                    }, StringBuilder::append)
                .toString();
        }
    }

}
