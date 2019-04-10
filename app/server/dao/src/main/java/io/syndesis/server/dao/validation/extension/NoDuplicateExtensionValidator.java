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
package io.syndesis.server.dao.validation.extension;

import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.validation.extension.ExtensionWithDomain;
import io.syndesis.common.model.validation.extension.NoDuplicateExtension;
import io.syndesis.server.dao.manager.DataManager;

public class NoDuplicateExtensionValidator implements ConstraintValidator<NoDuplicateExtension, Extension> {

    @Autowired
    private DataManager dataManager;

    @Override
    public void initialize(final NoDuplicateExtension validExtension) {
        // The annotation has no useful values
    }

    @Override
    public boolean isValid(final Extension value, final ConstraintValidatorContext context) {
        if (value instanceof ExtensionWithDomain) {
            return isValid((ExtensionWithDomain) value);
        }

        if (value.getExtensionId() == null) {
            return true;
        }

        Set<String> ids = dataManager.fetchIdsByPropertyValue(Extension.class, "extensionId", value.getExtensionId());
        if (value.getId().isPresent()) {
            ids.remove(value.getId().get());
        }

        for (String id : ids) {
            Extension other = dataManager.fetch(Extension.class, id);
            boolean installed = other.getStatus().isPresent() && other.getStatus().get() == Extension.Status.Installed;
            if (installed) {
                return false;
            }
        }
        return true;
    }

    private boolean isValid(final ExtensionWithDomain value) {
        Extension target = value.getTarget();
        if (target.getExtensionId() == null) {
            return true;
        }

        for (Extension other : value.getDomain()) {
            if (other.idEquals(target.getExtensionId())) {
                continue;
            }

            boolean installed = other.getStatus().isPresent() && other.getStatus().get() == Extension.Status.Installed;
            if (installed) {
                return false;
            }
        }

        return true;
    }
}
