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
package io.syndesis.common.model.support;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.action.StepAction;
import io.syndesis.common.model.action.StepDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.util.StringConstants;

@SuppressWarnings("PMD.GodClass")
public class Equivalencer implements StringConstants {

    static final Object NULL = "null";

    private Deque<EquivContext> failureContext;

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private boolean isSameRef(Object one, Object another) {
        return one == another;
    }

    private boolean push(String id, Class<?> klazz) {
        if (failureContext == null) {
            failureContext = new ArrayDeque<EquivContext>();
        }

        EquivContext ctx = new EquivContext(id, klazz);
        failureContext.push(ctx);
        return false;
    }

    private boolean push(String id, Class<?> klazz, String failingProperty, Object a, Object b) {
        if (failureContext == null) {
            failureContext = new ArrayDeque<EquivContext>();
        }

        EquivContext ctx = new EquivContext(id, klazz);
        ctx.setFail(failingProperty, a, b);
        failureContext.push(ctx);
        return false;
    }

    private EquivPair pair(Object a, Object b, String name) {
        return EquivPair.create(a, b, name);
    }

    private boolean compare(String objectName, Class<?> objectKind, EquivPair... pairs) {
        for (EquivPair pair : pairs) {
            if (! pair.isEqual()) {
                push(objectName, objectKind, pair.name(), pair.a, pair.b);
                return false;
            }
        }

        return true;
    }

    public String failureMessage() {
        if (failureContext == null) {
            return EMPTY_STRING;
        }

        String msg = "Reason: ";
        StringBuilder builder = new StringBuilder(msg);
        StringBuilder context = new StringBuilder();

        Iterator<EquivContext> iterator = failureContext.iterator();
        while(iterator.hasNext()) {
            EquivContext ctx = iterator.next();
            if (ctx.hasFailed()) {
                context.append(ctx.id());
                builder.append(ctx.getFailed());
                builder.append("Context: ").append(context.toString());
            } else {
                context.append(ctx.id()).append(SPACE).append(FORWARD_SLASH).append(SPACE);
            }
        }

        return builder.append(NEW_LINE).toString();
    }

    private <T> boolean equivalent(List<T> oneList, List<T> anotherList, Class<T> contentClass) {
        List<T> thisList = null;
        List<T> otherList = null;
        if (oneList.size() >= anotherList.size()) {
            thisList = oneList;
            otherList = anotherList;
        } else {
            thisList = anotherList;
            otherList = oneList;
        }

        for (int i = 0; i < thisList.size(); ++i) {
            T oneItem = thisList.get(i);
            T otherItem = null;

            if (otherList.size() > i) {
                otherItem = otherList.get(i);
            }

            if (! equivalent(oneItem, otherItem, contentClass)) {
                // Don't need to put context here
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("PMD.NPathComplexity")
    private <T> boolean equivalent(T one, T another, Class<T> tgtClass) {
        if (tgtClass.equals(Step.class)) {
            return equivalent((Step) one, (Step) another);
        }

        if (tgtClass.equals(Extension.class)) {
            return equivalent((Extension) one, (Extension) another);
        }

        if (tgtClass.equals(Integration.class)) {
            return equivalent((Integration) one, (Integration) another);
        }

        if (tgtClass.equals(Flow.class)) {
            return equivalent((Flow) one, (Flow) another);
        }

        if (tgtClass.equals(Connection.class)) {
            return equivalent((Connection) one, (Connection) another);
        }

        if (tgtClass.equals(Connector.class)) {
            return equivalent((Connector) one, (Connector) another);
        }

        if (tgtClass.equals(Action.class)) {
            return equivalent((Action) one, (Action) another);
        }

        if (tgtClass.equals(StepAction.class)) {
            return equivalent((StepAction) one, (StepAction) another);
        }

        if (tgtClass.equals(ConnectorAction.class)) {
            return equivalent((ConnectorAction) one, (ConnectorAction) another);
        }

        if (tgtClass.equals(StepDescriptor.class)) {
            return equivalent((StepDescriptor) one, (StepDescriptor) another);
        }

        if (tgtClass.equals(ConnectorAction.class)) {
            return equivalent((ConnectorAction) one, (ConnectorAction) another);
        }

        return false;
    }

    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     * <p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param another a {@link Step} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    @SuppressWarnings("PMD.NPathComplexity")
    public boolean equivalent(Step one, Step another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            return push(another.getName(), another.getClass(), another.getName(), NULL, another.getName());
        }

        if (another == null) {
            return push(one.getName(), one.getClass(), one.getName(), one.getName(), NULL);
        }

        if (! equivalent(one.getConnection().orElse(null), another.getConnection().orElse(null))) {
            return push(one.getName(), one.getClass());
        }

        if (! equivalent(one.getExtension().orElse(null), another.getExtension().orElse(null))) {
            return push(one.getName(), one.getClass());
        }

        if (! equivalent(one.getAction().orElse(null), another.getAction().orElse(null))) {
            return push(one.getName(), one.getClass());
        }

        return compare(one.getName(), one.getClass(),
                       pair(one.getStepKind(), another.getStepKind(), "step-kind"),
                       pair(one.getName(), another.getName(), "name"),
                       pair(one.getId(), another.getId(), "id"),
                       pair(one.getConfiguredProperties(), another.getConfiguredProperties(), "configured-properties"),
                       pair(one.getDependencies(), another.getDependencies(), "dependencies"),
                       pair(one.getMetadata(), another.getMetadata(), "metadata"));
    }

    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     * <p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param another a {@link Extension} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    @SuppressWarnings("PMD.NPathComplexity")
    public boolean equivalent(Extension one, Extension another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            return push(another.getName(), another.getClass(), another.getName(), NULL, another.getName());
        }

        if (another == null) {
            return push(one.getName(), one.getClass(), one.getName(), one.getName(), NULL);
        }

        if (! equivalent(one.getActions(), another.getActions(), Action.class)) {
            return push(one.getName(), one.getClass());
        }

        return compare(one.getName(), one.getClass(),
                       pair(one.getExtensionId(), another.getExtensionId(), "extension-id"),
                       pair(one.getSchemaVersion(), another.getSchemaVersion(), "schema-version"),
                       pair(one.getStatus(), another.getStatus(), "status"),
                       pair(one.getIcon(), another.getIcon(), "icon"),
                       pair(one.getDescription(), another.getDescription(), "description"),
                       pair(one.getUserId(), another.getUserId(), "user-id"),
                       pair(one.getExtensionType(), another.getExtensionType(), "extension-type"),
                       pair(one.getId(), another.getId(), "id"),
                       pair(one.getName(), another.getName(), "name"),
                       pair(one.getTags(), another.getTags(), "tags"),
                       pair(one.getProperties(), another.getProperties(), "properties"),
                       pair(one.getConfiguredProperties(), another.getConfiguredProperties(), "configured-properties"),
                       pair(one.getDependencies(), another.getDependencies(), "dependencies"),
                       pair(one.getMetadata(), another.getMetadata(), "metadata"));
    }

    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     *<p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param one a {@link Integration} to compare with
     * @param another a {@link Integration} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    @SuppressWarnings("PMD.NPathComplexity")
    public boolean equivalent(Integration one, Integration another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            return push(another.getName(), another.getClass(), another.getName(), NULL, another.getName());
        }

        if (another == null) {
            return push(one.getName(), one.getClass(), one.getName(), one.getName(), NULL);
        }

        if (! equivalent(one.getConnections(), another.getConnections(), Connection.class)) {
            return push(one.getName(), one.getClass());
        }

        if (! equivalent(one.getFlows(), another.getFlows(), Flow.class)) {
            return push(one.getName(), one.getClass());
        }

        return compare(one.getName(), one.getClass(),
                       pair(one.getKind(), another.getKind(), "id"),
                       pair(one.isDeleted(), another.isDeleted(), "is-deleted"),
                       pair(one.getResources(), another.getResources(), "resources"),
                       pair(one.getTags(), another.getTags(), "tags"),
                       pair(one.getDescription(), another.getDescription(), "description"),
                       pair(one.getName(), another.getName(), "name"));
    }

    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     *<p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param another a {@link Integration} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    @SuppressWarnings("PMD.NPathComplexity")
    public boolean equivalent(Flow one, Flow another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            return push(another.getName(), another.getClass(), another.getName(), NULL, another.getName());
        }

        if (another == null) {
            return push(one.getName(), one.getClass(), one.getName(), one.getName(), NULL);
        }

        if (! equivalent(one.getSteps(), another.getSteps(), Step.class)) {
            return push(one.getName(), one.getClass());
        }

        return compare(one.getName(), one.getClass(),
                       pair(one.getId(), another.getId(), "id"),
                       pair(one.getDescription(), another.getDescription(), "description"),
                       pair(one.getTags(), another.getTags(), "tags"),
                       pair(one.getName(), another.getName(), "name"));
    }

    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     * <p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param another a {@link Connection} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    public boolean equivalent(Connection one, Connection another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            return push(another.getName(), another.getClass(), another.getName(), NULL, another.getName());
        }

        if (another == null) {
            return push(one.getName(), one.getClass(), one.getName(), one.getName(), NULL);
        }

        if (! equivalent(one.getConnector().orElse(null), another.getConnector().orElse(null))) {
            return push(one.getName(), one.getClass());
        }

        return compare(one.getName(), one.getClass(),
                       pair(one.getId(), another.getId(), "id"),
                       pair(one.getOrganization(), another.getOrganization(), "organization"),
                       pair(one.getOrganizationId(), another.getOrganizationId(), "organization-id"),
                       pair(one.getConnectorId(), another.getConnectorId(), "connector-id"),
                       pair(one.getOptions(), another.getOptions(), "options"),
                       pair(one.getIcon(), another.getIcon(), "icon"),
                       pair(one.getDescription(), another.getDescription(), "description"),
                       pair(one.getUserId(), another.getUserId(), "user-id"),
                       pair(one.isDerived(), another.isDerived(), "is-derived"),
                       pair(one.getTags(), another.getTags(), "tags"),
                       pair(one.getName(), another.getName(), "name"),
                       pair(one.getConfiguredProperties(), another.getConfiguredProperties(), "configured-properties"));
    }

    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     * <p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param parentContext
     * @param one
     * @param another a {@link Connector} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    @SuppressWarnings("PMD.NPathComplexity")
    public boolean equivalent(Connector one, Connector another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            return push(another.getName(), another.getClass(), another.getName(), NULL, another.getName());
        }

        if (another == null) {
            return push(one.getName(), one.getClass(), one.getName(), one.getName(), NULL);
        }

        if (! equivalent(one.getActions(), another.getActions(), ConnectorAction.class)) {
            return push(one.getName(), one.getClass());
        }

        return compare(one.getName(), one.getClass(),
                       pair(one.getConnectorGroup(), another.getConnectorGroup(), "connector-group"),
                       pair(one.getConnectorGroupId(), another.getConnectorGroupId(), "connector-group-id"),
                       pair(one.getDescription(), another.getDescription(), "description"),
                       pair(one.getIcon(), another.getIcon(), "icon"),
                       pair(one.getKind(), another.getKind(), "kind"),
                       pair(one.getComponentScheme(), another.getComponentScheme(), "component-schema"),
                       pair(one.getConnectorFactory(), another.getConnectorFactory(), "connector-factory"),
                       pair(one.getConnectorCustomizers(), another.getConnectorCustomizers(), "connector-customizers"),
                       pair(one.getId(), another.getId(), "id"),
                       pair(one.getTags(), another.getTags(), "tags"),
                       pair(one.getName(), another.getName(), "name"),
                       pair(one.getProperties(), another.getProperties(), "properties"),
                       pair(one.getConfiguredProperties(), another.getConfiguredProperties(), "configured-properties"),
                       pair(one.getDependencies(), another.getDependencies(), "dependencies"),
                       pair(one.getMetadata(), another.getMetadata(), "metadata"));
    }

    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     * <p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param another a {@link Action} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    public boolean equivalent(Action one, Action another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            return push(another.getName(), another.getClass(), another.getName(), NULL, another.getName());
        }

        if (another == null) {
            return push(one.getName(), one.getClass(), one.getName(), one.getName(), NULL);
        }

        if (! one.getClass().equals(another.getClass())) {
            return push(one.getName(), one.getClass(), "action-classes", one.getClass(), another.getClass());
        }

        //
        // Passthrough so no context of its own required
        //
        if (one instanceof StepAction) {
            if (! equivalent((StepAction) one, (StepAction) another)) {
                return push(one.getName(), one.getClass());
            }
        } else if (another instanceof ConnectorAction &&
            !equivalent((ConnectorAction) one, (ConnectorAction) another)) {
                return push(one.getName(), one.getClass());
        }

        return true;
    }

    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     * <p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param another a {@link StepAction} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    public boolean equivalent(StepAction one, StepAction another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            return push(another.getName(), another.getClass(), another.getName(), NULL, another.getName());
        }

        if (another == null) {
            return push(one.getName(), one.getClass(), one.getName(), one.getName(), NULL);
        }

        if (! equivalent(one.getDescriptor(), another.getDescriptor())) {
            return push(one.getName(), one.getClass());
        }

        return compare(one.getName(), one.getClass(),
                       pair(one.getActionType(), another.getActionType(), "action-type"),
                       pair(one.getDescription(), another.getDescription(), "description"),
                       pair(one.getPattern(), another.getPattern(), "pattern"),
                       pair(one.getId(), another.getId(), "id"),
                       pair(one.getName(), another.getName(), "name"),
                       pair(one.getTags(), another.getTags(), "tags"),
                       pair(one.getMetadata(), another.getMetadata(), "metadata"));
    }

    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     * <p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param another a {@link ConnectorAction} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    public boolean equivalent(ConnectorAction one, ConnectorAction another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            return push(another.getName(), another.getClass(), another.getName(), NULL, another.getName());
        }

        if (another == null) {
            return push(one.getName(), one.getClass(), one.getName(), one.getName(), NULL);
        }

        if (! equivalent(one.getDescriptor(), another.getDescriptor())) {
            return push(one.getName(), one.getClass());
        }

        return compare(one.getName(), one.getClass(),
                       pair(one.getActionType(), another.getActionType(), "action-type"),
                       pair(one.getDescription(), another.getDescription(), "description"),
                       pair(one.getPattern(), another.getPattern(), "pattern"),
                       pair(one.getId(), another.getId(), "id"),
                       pair(one.getName(), another.getName(), "name"),
                       pair(one.getTags(), another.getTags(), "tags"),
                       pair(one.getMetadata(), another.getMetadata(), "metadata"));
    }

    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     * <p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param another a {@link StepDescriptor} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    public boolean equivalent(StepDescriptor one, StepDescriptor another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            return push(another.getEntrypoint(), another.getClass(), another.getEntrypoint(), NULL, another.getEntrypoint());
        }

        if (another == null) {
            return push(one.getEntrypoint(), one.getClass(), one.getEntrypoint(), one.getEntrypoint(), NULL);
        }

        return compare(one.getEntrypoint(), one.getClass(),
                       pair(one.getKind(), another.getKind(), "kind"),
                       pair(one.getEntrypoint(), another.getEntrypoint(), "entry-point"),
                       pair(one.getResource(), another.getResource(), "resource"),
                       pair(one.getInputDataShape(), another.getInputDataShape(), "input-data-shape"),
                       pair(one.getOutputDataShape(), another.getOutputDataShape(), "output-data-shape"),
                       pair(one.getPropertyDefinitionSteps(), another.getPropertyDefinitionSteps(), "property-definition-steps"));
    }


    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     * <p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param another a {@link ConnectorDescriptor} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public boolean equivalent(ConnectorDescriptor one, ConnectorDescriptor another) {
        if (one == another) {
            return true;
        }

        if (one == null) {
            String identifier = another.getConnectorId() + "-Descriptor";
            return push(identifier, another.getClass(), identifier, NULL, identifier);
        }

        String identifier = one.getConnectorId() + "-Descriptor";
        if (another == null) {
            return push(identifier, one.getClass(), identifier, identifier, NULL);
        }

        return compare(identifier, one.getClass(),
                       pair(one.getConnectorId(), another.getConnectorId(), "connector-id"),
                       pair(one.getCamelConnectorGAV(), another.getCamelConnectorGAV(), "camel-connector-gav"),
                       pair(one.getCamelConnectorPrefix(), another.getCamelConnectorPrefix(), "camel-connector-prefix"),
                       pair(one.getComponentScheme(), another.getComponentScheme(), "component-scheme"),
                       pair(one.getConnectorFactory(), another.getConnectorFactory(), "connector-factory"),
                       pair(one.getConnectorCustomizers(), another.getConnectorCustomizers(), "connector-customizers"),
                       pair(one.getInputDataShape(), another.getInputDataShape(), "input-data-shape"),
                       pair(one.getOutputDataShape(), another.getOutputDataShape(), "output-data-shape"),
                       pair(one.getPropertyDefinitionSteps(), another.getPropertyDefinitionSteps(), "property-defn-steps"),
                       pair(one.getConfiguredProperties(), another.getConfiguredProperties(), "configured-properties"));
    }
}
