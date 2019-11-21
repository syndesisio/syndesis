/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.dv.datasources;

import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.teiid.translator.ExecutionFactory;
import org.teiid.translator.Translator;

public class ExternalSource {

    private ExternalSource() {}

    public static Class<? extends ExecutionFactory<?, ?>> translatorClass(String translatorName, String basePackage) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Translator.class));
        Class<? extends ExecutionFactory<?, ?>> clazz = findTranslatorInPackage(translatorName, provider,
                "org.teiid.translator");
        if (clazz == null) {
            clazz = findTranslatorInPackage(translatorName, provider,basePackage);
        }
        return clazz;
    }

    static Class<? extends ExecutionFactory<?, ?>> findTranslatorInPackage(String translatorName,
            ClassPathScanningCandidateComponentProvider provider, String packageName) {
        Set<BeanDefinition> components = provider.findCandidateComponents(packageName);
        for (BeanDefinition c : components) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends ExecutionFactory<?, ?>> clazz = (Class<? extends ExecutionFactory<?, ?>>) Class
                .forName(c.getBeanClassName());
                String name = clazz.getAnnotation(Translator.class).name();
                if (name.equals(translatorName)) {
                    return clazz;
                }
            } catch (ClassNotFoundException | SecurityException | IllegalArgumentException e) {
                throw new IllegalStateException("Error loading translators", e);
            }
        }
        return null;
    }
}
