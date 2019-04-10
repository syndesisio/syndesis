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

    private static final String ONE_NULL = "one-null";

    private static final String ANOTHER_NULL = "another-null";

    static final Object NULL = "null";

    private static final Object NOT_NULL = "not-null";

    private EquivContext topLevel;

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private boolean isSameRef(Object one, Object another) {
        return one == another;
    }

    private EquivContext identifier(EquivContext parent, String id, Class<?> klazz) {
        if (parent == null) {
            topLevel = new EquivContext(id, klazz);
            return topLevel;
        } else {
            return parent.addChild(id, klazz);
        }
    }

    private EquivPair pair(Object a, Object b, String name) {
        return EquivPair.create(a, b, name);
    }

    private boolean compare(EquivContext context, EquivPair... pairs) {
        for (EquivPair pair : pairs) {
            if (! pair.isEqual()) {
                context.setFail(pair.name(), pair.a, pair.b);
                return false;
            }
        }

        return true;
    }

    private String layer(EquivContext context) {
        if (isSameRef(context, topLevel)) {
            return context.id();
        }

        return SPACE + CLOSE_ANGLE_BRACKET + SPACE + context.id();
    }

    private String message(EquivContext context) {
        StringBuilder msgBuilder = new StringBuilder();

        if (context.hasFailed()) {
            msgBuilder
                .append(layer(context))
                .append(DOLLAR_SIGN)
                .append(context.getFailed());
        } else {
            for (EquivContext child : context.children()) {
                String msg = message(child);
                if (msg.isEmpty()) {
                    continue;
                }

                msgBuilder
                    .append(layer(context))
                    .append(msg);
            }
        }

        return msgBuilder.toString();
    }

    public String message() {
        if (topLevel == null) {
            return EMPTY_STRING;
        }

        return message(topLevel);
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
    public boolean equivalent(EquivContext parentContext, Step one, Step another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            EquivContext context = identifier(parentContext, another.getName(), another.getClass());
            context.setFail(ONE_NULL, NULL, NOT_NULL);
            return false;
        }

        EquivContext context = identifier(parentContext, one.getName(), one.getClass());
        if (another == null) {
            context.setFail(ANOTHER_NULL, NOT_NULL, NULL);
            return false;
        }

        Connection myConnection = one.getConnection().orElse(null);
        Connection anotherConnection = another.getConnection().orElse(null);
        if (myConnection == null) {
            if (anotherConnection != null) {
                context.setFail("connection", NULL, NOT_NULL);
                return false;
            }
        } else if (! equivalent(context, myConnection, anotherConnection)) {
            return false;
        }

        Extension myExtension = one.getExtension().orElse(null);
        Extension anotherExtension = another.getExtension().orElse(null);
        if (myExtension == null) {
            if (anotherExtension != null) {
                return false;
            }
        } else if (! equivalent(context, myExtension, anotherExtension)) {
            return false;
        }

        Action myAction = one.getAction().orElse(null);
        Action anotherAction = another.getAction().orElse(null);
        if (myAction == null) {
            if (anotherAction != null) {
                context.setFail("action", NULL, NOT_NULL);
                return false;
            }
        } else if (! equivalent(context, myAction, anotherAction)) {
            return false;
        }

        return compare(context,
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
    public boolean equivalent(EquivContext parentContext, Extension one, Extension another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            EquivContext context = identifier(parentContext, another.getName(), another.getClass());
            context.setFail(ONE_NULL, NULL, NOT_NULL);
            return false;
        }

        EquivContext context = identifier(parentContext, one.getName(), one.getClass());
        if (another == null) {
            context.setFail(ANOTHER_NULL, NOT_NULL, NULL);
            return false;
        }

        List<Action> myActions = one.getActions();
        if (myActions == null) {
            if (another.getActions() != null) {
                return false;
            }
        } else {
            for (Action myAction : myActions) {
                Action anotherAction = another.findActionById(myAction.getId().get()).orElse(null);
                if (! equivalent(context, myAction, anotherAction)) {
                    return false;
                }
            }
        }

        return compare(context,
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
    public boolean equivalent(EquivContext parentContext, Integration one, Integration another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            EquivContext context = identifier(parentContext, another.getName(), another.getClass());
            context.setFail(ONE_NULL, NULL, NOT_NULL);
            return false;
        }

        EquivContext context = identifier(parentContext, one.getName(), one.getClass());
        if (another == null) {
            context.setFail(ANOTHER_NULL, NOT_NULL, NULL);
            return false;
        }

        List<Connection> myConnections = one.getConnections();
        if (myConnections == null) {
            if (another.getConnections() != null) {
                context.setFail("connections", NULL, NOT_NULL);
                return false;
            }
        } else {
            for (Connection myConnection : myConnections) {
                Connection anotherConnection = another.findConnectionById(myConnection.getId().get()).orElse(null);
                if (! equivalent(context, myConnection, anotherConnection)) {
                    return false;
                }
            }
        }

        List<Flow> myFlows = one.getFlows();
        if (myFlows == null) {
            if (another.getFlows() != null) {
                context.setFail("flows", NULL, NOT_NULL);
                return false;
            }
        } else {
            for (Flow myFlow : myFlows) {
                Flow anotherFlow = another.findFlowById(myFlow.getId().get()).orElse(null);
                if (! equivalent(context, myFlow, anotherFlow)) {
                    return false;
                }
            }
        }

        return compare(context,
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
    public boolean equivalent(EquivContext parentContext, Flow one, Flow another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            EquivContext context = identifier(parentContext, another.getName(), another.getClass());
            context.setFail(ONE_NULL, NULL, NOT_NULL);
            return false;
        }

        EquivContext context = identifier(parentContext, one.getName(), one.getClass());
        if (another == null) {
            context.setFail(ANOTHER_NULL, NOT_NULL, NULL);
            return false;
        }

        List<Step> mySteps = one.getSteps();
        if (mySteps == null) {
            if (another.getSteps() != null) {
                context.setFail("steps", NULL, NOT_NULL);
                return false;
            }
        } else {
            for (Step myStep : mySteps) {
                Step anotherStep = another.findStepById(myStep.getId().get()).orElse(null);
                if (! equivalent(context, myStep, anotherStep)) {
                    return false;
                }
            }
        }

        return compare(context,
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
    public boolean equivalent(EquivContext parentContext, Connection one, Connection another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            EquivContext context = identifier(parentContext, another.getName(), another.getClass());
            context.setFail(ONE_NULL, NULL, NOT_NULL);
            return false;
        }

        EquivContext context = identifier(parentContext, one.getName(), one.getClass());
        if (another == null) {
            context.setFail(ANOTHER_NULL, NOT_NULL, NULL);
            return false;
        }

        Connector myConnector = one.getConnector().orElse(null);
        Connector anotherConnector = another.getConnector().orElse(null);

        if (myConnector == null) {
            if (anotherConnector != null) {
                context.setFail("connector", NULL, NOT_NULL);
                return false;
            }
        } else if (! equivalent(context, myConnector, anotherConnector)) {
            return false;
        }

        return compare(context,
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
    public boolean equivalent(EquivContext parentContext, Connector one, Connector another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            EquivContext context = identifier(parentContext, another.getName(), another.getClass());
            context.setFail(ONE_NULL, NULL, NOT_NULL);
            return false;
        }

        EquivContext context = identifier(parentContext, one.getName(), one.getClass());
        if (another == null) {
            context.setFail(ANOTHER_NULL, NOT_NULL, NULL);
            return false;
        }

        List<ConnectorAction> myActions = one.getActions();
        if (myActions == null) {
            if (another.getActions() != null) {
                context.setFail("actions", NULL, NOT_NULL);
                return false;
            }
        } else {
            for (ConnectorAction myAction : myActions) {
                ConnectorAction anotherAction = another.findActionById(myAction.getId().get()).orElse(null);
                if (! equivalent(context, myAction, anotherAction)) {
                    return false;
                }
            }
        }

        return compare(context,
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
    public boolean equivalent(EquivContext parentContext, Action one, Action another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            EquivContext context = identifier(parentContext, another.getName(), another.getClass());
            context.setFail(ONE_NULL, NULL, NOT_NULL);
            return false;
        }

        if (another == null) {
            EquivContext context = identifier(parentContext, one.getName(), one.getClass());
            context.setFail(ANOTHER_NULL, NOT_NULL, NULL);
            return false;
        }

        if (! one.getClass().equals(another.getClass())) {
            EquivContext context = identifier(parentContext, one.getName(), one.getClass());
            context.setFail("action-classes", one.getClass(), another.getClass());
            return false;
        }

        //
        // Passthrough so no context of its own required
        //
        if (one instanceof StepAction) {
            return equivalent(parentContext, (StepAction) one, (StepAction) another);
        } else if (another instanceof ConnectorAction) {
            return equivalent(parentContext, (ConnectorAction) one, (ConnectorAction) another);
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
     * @param another a {@link StepAction} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    public boolean equivalent(EquivContext parentContext, StepAction one, StepAction another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            EquivContext context = identifier(parentContext, another.getName(), another.getClass());
            context.setFail(ONE_NULL, NULL, NOT_NULL);
            return false;
        }

        EquivContext context = identifier(parentContext, one.getName(), one.getClass());
        if (another == null) {
            context.setFail(ANOTHER_NULL, NOT_NULL, NULL);
            return false;
        }

        StepDescriptor myDescriptor = one.getDescriptor();
        StepDescriptor anotherDescriptor = another.getDescriptor();
        if (myDescriptor == null) {
            if (anotherDescriptor != null) {
                context.setFail("descriptor", NULL, NOT_NULL);
                return false;
            }
        } else if (! equivalent(context, myDescriptor, anotherDescriptor)) {
            return false;
        }

        return compare(context,
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
    public boolean equivalent(EquivContext parentContext, ConnectorAction one, ConnectorAction another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            EquivContext context = identifier(parentContext, another.getName(), another.getClass());
            context.setFail(ONE_NULL, NULL, NOT_NULL);
            return false;
        }

        EquivContext context = identifier(parentContext, one.getName(), one.getClass());
        if (another == null) {
            context.setFail(ANOTHER_NULL, NOT_NULL, NULL);
            return false;
        }

        ConnectorDescriptor myDescriptor = one.getDescriptor();
        ConnectorDescriptor anotherDescriptor = another.getDescriptor();
        if (myDescriptor == null) {
            if (anotherDescriptor != null) {
                context.setFail("descriptor", NULL, NOT_NULL);
                return false;
            }
        } else if (! equivalent(context, myDescriptor, anotherDescriptor)) {
            return false;
        }

        return compare(context,
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
    public boolean equivalent(EquivContext parentContext, StepDescriptor one, StepDescriptor another) {
        if (isSameRef(one, another)) {
            return true;
        }

        if (one == null) {
            EquivContext context = identifier(parentContext, null, another.getClass());
            context.setFail(ONE_NULL, NULL, NOT_NULL);
            return false;
        }

        EquivContext context = identifier(parentContext, null, one.getClass());
        if (another == null) {
            context.setFail(ANOTHER_NULL, NOT_NULL, NULL);
            return false;
        }

        return compare(context,
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
    public boolean equivalent(EquivContext parentContext, ConnectorDescriptor one, ConnectorDescriptor another) {
        if (one == another) {
            return true;
        }

        if (one == null) {
            EquivContext context = identifier(parentContext, null, another.getClass());
            context.setFail(ONE_NULL, NULL, NOT_NULL);
            return false;
        }

        EquivContext context = identifier(parentContext, null, one.getClass());
        if (another == null) {
            context.setFail(ANOTHER_NULL, NOT_NULL, NULL);
            return false;
        }

        return compare(context,
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
