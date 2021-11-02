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
package io.syndesis.connector.sql.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;
import org.testcontainers.DockerClientFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageCmd;
import com.github.dockerjava.api.exception.NotFoundException;

public class IfImagePresentCondition implements ExecutionCondition {

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @ExtendWith(IfImagePresentCondition.class)
    static @interface IfImagePresent {
        String value();
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
        return AnnotationUtils.findAnnotation(context.getElement(), IfImagePresent.class)
            .map(i -> {
                final String imageName = i.value();
                if (imagePresent(imageName)) {
                    return ConditionEvaluationResult.enabled("Image `" + imageName + "` is present, enabling test");
                }

                return ConditionEvaluationResult.disabled("Image `" + imageName + "` is not present, disabling test");
            })
            .orElse(ConditionEvaluationResult.enabled("No @IfImagePresent annotation, free to proceed"));
    }

    private static boolean imagePresent(final String imageName) {
        // we must not close the global DockerClient
        @SuppressWarnings("resource")
        final DockerClient client = DockerClientFactory.lazyClient();
        try (InspectImageCmd command = client.inspectImageCmd(imageName)) {
            command.exec();
            return true;
        } catch (final NotFoundException ignored) {
            return false;
        }
    }

}
