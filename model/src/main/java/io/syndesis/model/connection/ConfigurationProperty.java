/**
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
package io.syndesis.model.connection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.WithTags;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = ConfigurationProperty.Builder.class)
public interface ConfigurationProperty extends WithTags {

    String getKind();
    String getDisplayName();
    String getGroup();
    String getLabel();
    Boolean getRequired();
    String getType();
    String getJavaType();
    Boolean getDeprecated();
    Boolean getSecret();
    Boolean getComponentProperty();
    String getDescription();
    String getDefaultValue();

    class Builder extends ImmutableConfigurationProperty.Builder {
    }

}
