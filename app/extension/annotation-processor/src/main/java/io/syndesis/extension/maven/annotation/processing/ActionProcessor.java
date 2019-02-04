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
package io.syndesis.extension.maven.annotation.processing;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Locale;
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

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressWarnings("PMD")
@SupportedSourceVersion(value = SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
    ActionProcessor.SYNDESIS_ANNOTATION_CLASS_NAME
})
public class ActionProcessor extends AbstractProcessor {
    public static final String SYNDESIS_ANNOTATION_CLASS_NAME = "io.syndesis.extension.api.annotations.Action";
    public static final String SYNDESIS_PROPERTY_ANNOTATION_CLASS_NAME = "io.syndesis.extension.api.annotations.ConfigurationProperty";
    public static final String SYNDESIS_PROPERTY_ENUM_ANNOTATION_CLASS_NAME = "io.syndesis.extension.api.annotations.ConfigurationProperty$PropertyEnum";
    public static final String SYNDESIS_DATA_SHAPE_VARIANT_CLASS_NAME = "io.syndesis.extension.api.annotations.DataShape$Variant";
    public static final String SYNDESIS_DATA_SHAPE_META_CLASS_NAME = "io.syndesis.extension.api.annotations.DataShape$Meta";

    public static final String SYNDESIS_STEP_CLASS_NAME = "io.syndesis.extension.api.Step";
    public static final String CAMEL_HANDLER_ANNOTATION_CLASS_NAME = "org.apache.camel.Handler";
    public static final String CAMEL_ROUTE_BUILDER_CLASS_NAME_ = "org.apache.camel.builder.RouteBuilder";
    public static final String BEAN_ANNOTATION_CLASS_NAME = "org.springframework.context.annotation.Bean";

    private ObjectMapper mapper;
    private Class<? extends Annotation> annotationClass;
    private Class<? extends Annotation> propertyAnnotationClass;
    private Class<? extends Annotation> propertyEnumAnnotationClass;
    private Class<? extends Annotation> dataShapeVariantAnnotationClass;
    private Class<? extends Annotation> dataShapeMetaAnnotationClass;
    private Class<? extends Annotation> beanAnnotationClass;
    private Class<? extends Annotation> handlerAnnotationClass;
    private Class<? extends Annotation> routeBuilderClass;
    private Class<?> stepClass;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mapper = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);;

        annotationClass = mandatoryFindClass(SYNDESIS_ANNOTATION_CLASS_NAME);
        propertyAnnotationClass = mandatoryFindClass(SYNDESIS_PROPERTY_ANNOTATION_CLASS_NAME);
        propertyEnumAnnotationClass = mandatoryFindClass(SYNDESIS_PROPERTY_ENUM_ANNOTATION_CLASS_NAME);
        dataShapeVariantAnnotationClass = mandatoryFindClass(SYNDESIS_DATA_SHAPE_VARIANT_CLASS_NAME);
        dataShapeMetaAnnotationClass = mandatoryFindClass(SYNDESIS_DATA_SHAPE_META_CLASS_NAME);
        stepClass = findClass(SYNDESIS_STEP_CLASS_NAME);
        beanAnnotationClass = findClass(BEAN_ANNOTATION_CLASS_NAME);
        handlerAnnotationClass = mandatoryFindClass(CAMEL_HANDLER_ANNOTATION_CLASS_NAME);
        routeBuilderClass = mandatoryFindClass(CAMEL_ROUTE_BUILDER_CLASS_NAME_);
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
                    ObjectNode root = mapper.createObjectNode();

                    gatherProperties(root, annotatedElement.getAnnotation(annotationClass));

                    claimed = augmentProperties(root, (TypeElement) annotatedElement);
                    addActionProperties(root, annotatedElement);

                    File file = obtainResourceFile(annotatedElement);
                    if (file != null) {
                        mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
                    }
                } catch (IOException|InvocationTargetException|IllegalAccessException|NoSuchMethodException e){
                    claimed = false;
                }
            } else if (annotatedElement.getKind() == ElementKind.METHOD) {
                try {
                    ObjectNode root = mapper.createObjectNode();

                    gatherProperties(root, annotatedElement.getAnnotation(annotationClass));

                    augmentProperties(root, (ExecutableElement) annotatedElement);
                    addActionProperties(root, annotatedElement);

                    File file = obtainResourceFile(annotatedElement);
                    if (file != null) {
                        mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
                    }
                } catch (IOException|InvocationTargetException|IllegalAccessException|NoSuchMethodException e){
                    claimed = false;
                }
            } else {
                claimed = true;
            }
        }

        return claimed;
    }

    // *****************************************
    // Annotation processing
    // *****************************************

    /**
     * Explicitly add properties that elude reflection implicit strategy
     * @param element
     * @param root
     */
    private boolean augmentProperties(ObjectNode root, TypeElement element) throws InvocationTargetException, IllegalAccessException {
        final Elements elements = processingEnv.getElementUtils();
        final TypeElement stepTypeElement = elements.getTypeElement(stepClass.getName());
        final TypeElement routeBuilderTypeElement = elements.getTypeElement(routeBuilderClass.getName());

        if (stepTypeElement != null && processingEnv.getTypeUtils().isAssignable(element.asType(), stepTypeElement.asType())) {
            root.put("kind", "STEP");
            root.put("entrypoint", element.getQualifiedName().toString());

            // Let's search for fields annotated with ConfigurationProperty
            for (Element field: element.getEnclosedElements()) {
                if (field.getKind() == ElementKind.FIELD) {
                    addActionProperties(root, field);
                }
            }

            return true;
        } else if (routeBuilderTypeElement != null && processingEnv.getTypeUtils().isAssignable(element.asType(), routeBuilderTypeElement.asType())) {
            root.put("kind", "ENDPOINT");
            root.put("resource", "class:" + element.getQualifiedName().toString());

            // Let's search for fields annotated with ConfigurationProperty
            for (Element field: element.getEnclosedElements()) {
                if (field.getKind() == ElementKind.FIELD) {
                    addActionProperties(root, field);
                }
            }
        } else {
            root.put("kind", "BEAN");
            root.put("entrypoint", element.getQualifiedName().toString());

            for (Element method: element.getEnclosedElements()) {
                if (method.getAnnotation(handlerAnnotationClass) != null) {
                    // Process method
                    augmentProperties(root, (ExecutableElement)method);
                    addActionProperties(root, method);

                    // Found a method annotated with Handler, let's search for
                    // fields annotated with ConfigurationProperty
                    for (Element field: element.getEnclosedElements()) {
                        if (field.getKind() == ElementKind.FIELD) {
                            addActionProperties(root, field);
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
     * @param root
     * @param element
     */
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    private void augmentProperties(ObjectNode root, ExecutableElement element) {
        final TypeElement typedElement = (TypeElement) element.getEnclosingElement();

        if (beanAnnotationClass != null && element.getAnnotation(beanAnnotationClass) != null) {
            root.put("kind", "ENDPOINT");
        } else {
            root.put("kind", "BEAN");
            root.put("entrypoint", typedElement.getQualifiedName().toString() + "::" + element.getSimpleName());
        }
    }

    /**
     * Add action properties to the global properties.
     * @param root
     * @param element
     */
    private void addActionProperties(ObjectNode root, Element element) throws InvocationTargetException, IllegalAccessException {

        Annotation[] annotations = element.getAnnotationsByType(propertyAnnotationClass);
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            ObjectNode propertyNode = mapper.createObjectNode();

            gatherProperties(propertyNode, annotation);

            if (element.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement)element;

                TypeMirror typeMirror = field.asType();
                TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(typeMirror.toString());
                String javaType = typeMirror.toString();
                String type = propertyNode.get("type").asText();

                if (!propertyNode.has("enums")) {
                    // don't auto detect enum if enums are set through annotations
                    if (typeElement != null && typeElement.getKind() == ElementKind.ENUM) {
                        for (Element enumElement : typeElement.getEnclosedElements()) {
                            if (enumElement.getKind() == ElementKind.ENUM_CONSTANT) {
                                ObjectNode enumNode = mapper.createObjectNode();

                                writeIfNotEmpty(enumNode, "label", enumElement.toString());
                                writeIfNotEmpty(enumNode, "value", enumElement.toString());

                                propertyNode.withArray("enums").add(enumNode);
                            }
                        }

                        javaType = String.class.getName();
                        type = String.class.getName();
                    }
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

                writeIfNotEmpty(propertyNode, "javaType", javaType);
                writeIfNotEmpty(propertyNode, "type", type);
            }

            root.withArray("properties").add(propertyNode);
        }
    }

    private void gatherProperties(ObjectNode root, Annotation annotation) throws InvocationTargetException, IllegalAccessException  {
        Method[] methods = annotation.annotationType().getDeclaredMethods();
        for (Method m : methods) {
            if (m.getReturnType().isAnnotation()) {
                ObjectNode node = root.putObject(m.getName());

                gatherProperties(node, (Annotation)m.invoke(annotation));
            } else {
                writeIfNotEmpty(root, m.getName(), m.invoke(annotation));
            }
        }
    }

    private void writeIfNotEmpty(ObjectNode root, String key, Object value) throws InvocationTargetException, IllegalAccessException {
        if(value != null && !"".equals(value.toString().trim())) {
            if(value instanceof String[]){
                String[] arr = (String[])value;

                if(arr.length > 0) {
                    ArrayNode arrayNode = root.putArray(key);
                    for (String val: arr) {
                        arrayNode.add(val);
                    }
                }
            } else if(Object[].class.isInstance(value)) {
                Object[] array = (Object[]) value;
                for (int i = 0; i < array.length; i++) {
                    if (propertyEnumAnnotationClass.isInstance(array[i]) ||
                        dataShapeVariantAnnotationClass.isInstance(array[i]) ||
                        dataShapeMetaAnnotationClass.isInstance(array[i])) {

                        Annotation annotation = (Annotation) array[i];
                        ObjectNode node = mapper.createObjectNode();

                        gatherProperties(node, annotation);

                        root.withArray(key).add(node);
                    }
                }
            } else {
                root.put(key, value.toString());
            }
        }
    }

    // *****************************************
    // Helpers
    // *****************************************

    /**
     * Helper method to produce class output text file using the given handler
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    private File obtainResourceFile(Element element) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
            .append(".json")
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

    /**
     * Returns the canonical class name by removing any generic type information.
     */
    private static String canonicalClassName(String className) {
        // remove generics
        int pos = className.indexOf('<');
        if (pos != -1) {
            return className.substring(0, pos);
        } else {
            return className;
        }
    }

    private void info(String format, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format(format, args));
    }

    private void warning(String format, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, String.format(format, args));
    }

    private void error(String format, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(format, args));
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
    private static final class Names {
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
