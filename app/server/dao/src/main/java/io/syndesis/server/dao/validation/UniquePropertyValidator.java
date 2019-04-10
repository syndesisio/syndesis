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
package io.syndesis.server.dao.validation;

import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.validation.UniqueProperty;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.factory.annotation.Autowired;

public class UniquePropertyValidator implements ConstraintValidator<UniqueProperty, WithId<?>> {

    @Autowired
    DataManager dataManager;

    private String property;

    @Override
    public void initialize(final UniqueProperty uniqueProperty) {
        property = uniqueProperty.value();
    }

    @Override
    public boolean isValid(final WithId<?> value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        final PropertyAccessor bean = new BeanWrapperImpl(value);

        final String propertyValue = String.valueOf(bean.getPropertyValue(property));

        @SuppressWarnings({"rawtypes", "unchecked"})
        final Class<WithId> modelClass = (Class) value.getKind().modelClass;

        @SuppressWarnings("unchecked")
        final Set<String> ids = dataManager.fetchIdsByPropertyValue(modelClass, property, propertyValue);

        final boolean isUnique = ids.isEmpty() || value.getId().map(id -> ids.contains(id)).orElse(false);

        if (!isUnique) {
            if (ids.stream().allMatch(id -> consideredValidByException(modelClass, id))) {
                return true;
            }

            context.disableDefaultConstraintViolation();
            context.unwrap(HibernateConstraintValidatorContext.class).addExpressionVariable("nonUnique", propertyValue)
                .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode(property).addConstraintViolation();
        }

        return isUnique;
    }

    boolean consideredValidByException(@SuppressWarnings("rawtypes") final Class<WithId> modelClass,
        final String id) {
        @SuppressWarnings("unchecked")
        final WithId<?> modelInstance = dataManager.fetch(modelClass, id);

        // if we're looking at Integration then we need to make sure that
        // the Integration in question is not deleted
        if (modelInstance instanceof Integration) {
            return ((Integration) modelInstance).isDeleted();
        }

        return false;
    }

}
