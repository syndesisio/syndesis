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
package io.syndesis.server.api.generator.soap.parser;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.cxf.common.xmlschema.SchemaCollection;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroup;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroupMember;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef;
import org.apache.ws.commons.schema.XmlSchemaAttributeOrGroupRef;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.apache.ws.commons.schema.XmlSchemaItemWithRef;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeList;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeUnion;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.apache.ws.commons.schema.utils.XmlSchemaNamedWithForm;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

/**
 * Extracts Xml Schema types and elements as top level items,
 * inlines structure with anonymous types
 * and resolves 'ref' and 'type' attributes.
 */
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.GodClass", "PMD.CyclomaticComplexity"})
class XmlSchemaExtractor {

    // names of properties to exclude from shallow copy
    private static final Map<Class<?>, List<String>> BLACK_LISTED_PROPERTIES_MAP = new HashMap<>();

    // ordered entries for schema type handlers
    private static final Map<Predicate<XmlSchemaObjectBase>, TriConsumer<XmlSchemaExtractor, XmlSchemaObjectBase, XmlSchemaObjectBase>> HANDLER_MAP = new LinkedHashMap<>();

    private final SchemaCollection targetSchemas;
    private final SchemaCollection sourceSchemas;

    // stack to hold XmlSchemaObjects to be copied
    private final Deque<ObjectPair<? super XmlSchemaObjectBase>> objectPairStack;

    // stack to save current namespace when creating object pairs
    private final Deque<String> currentNamespace;

    static {
        // initialize properties map
        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaAttribute.class, Arrays.asList("name", "form", "use", "schemaType", "schemaTypeName"));
        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaAttributeGroup.class, Arrays.asList("name", "attributes"));

        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaSimpleContent.class, Arrays.asList("name", "content"));
        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaComplexContent.class, Arrays.asList("name", "content"));

        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaSimpleTypeList.class, Arrays.asList("itemType", "itemTypeName"));
        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaSimpleTypeRestriction.class, Arrays.asList("baseType", "baseTypeName"));
        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaSimpleTypeUnion.class, Arrays.asList("baseTypes", "memberTypesSource", "memberTypesQNames"));

        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaSimpleContentExtension.class, Arrays.asList("attributes", "baseTypeName"));
        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaSimpleContentRestriction.class, Arrays.asList("attribute", "baseType", "baseTypeName"));
        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaComplexContentExtension.class, Arrays.asList("attributes", "baseTypeName", "particle"));
        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaComplexContentRestriction.class, Arrays.asList("attributes", "baseTypeName", "particle"));

        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaElement.class, Arrays.asList("name", "form", "schemaType", "schemaTypeName"));
        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaSimpleType.class, Arrays.asList("name", "content"));
        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaComplexType.class, Arrays.asList("name", "attributes", "contentModel", "particle"));

        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaAll.class, Collections.singletonList("items"));
        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaChoice.class, Collections.singletonList("items"));
        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaSequence.class, Collections.singletonList("items"));
        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaGroupRef.class, Collections.singletonList("refName"));

        BLACK_LISTED_PROPERTIES_MAP.put(XmlSchemaGroup.class, Collections.singletonList("particle"));

        // initialize handler map
        HANDLER_MAP.put(s -> s instanceof XmlSchemaItemWithRef && ((XmlSchemaItemWithRef<?>) s).isRef(),
                (x, t, s) -> x.handleItemWithRef((XmlSchemaItemWithRef<?>) t, (XmlSchemaItemWithRef<?>) s));
        HANDLER_MAP.put(XmlSchemaAttribute.class::isInstance, (x, t, s) -> x.handleAttribute((XmlSchemaAttribute) t,
                (XmlSchemaAttribute) s));
        HANDLER_MAP.put(XmlSchemaAttributeGroup.class::isInstance, (x, t, s) -> x.handleAttributeGroup((XmlSchemaAttributeGroup) t,
                (XmlSchemaAttributeGroup) s));
        HANDLER_MAP.put(XmlSchemaElement.class::isInstance, (x, t, s) -> x.handleElement((XmlSchemaElement) t,
                (XmlSchemaElement) s));
        HANDLER_MAP.put(XmlSchemaSimpleType.class::isInstance, (x, t, s) -> x.handleSimpleType((XmlSchemaSimpleType) t
                , (XmlSchemaSimpleType) s));
        HANDLER_MAP.put(XmlSchemaSimpleTypeList.class::isInstance,
                (x, t, s) -> x.handleSimpleTypeList((XmlSchemaSimpleTypeList) t, (XmlSchemaSimpleTypeList) s));
        HANDLER_MAP.put(XmlSchemaSimpleTypeRestriction.class::isInstance,
                (x, t, s) -> x.handleSimpleTypeRestriction((XmlSchemaSimpleTypeRestriction) t,
                        (XmlSchemaSimpleTypeRestriction) s));
        HANDLER_MAP.put(XmlSchemaSimpleTypeUnion.class::isInstance,
                (x, t, s) -> x.handleSimpleTypeUnion((XmlSchemaSimpleTypeUnion) t, (XmlSchemaSimpleTypeUnion) s));
        HANDLER_MAP.put(XmlSchemaSimpleContentExtension.class::isInstance,
                (x, t, s) -> x.handleSimpleContentExtension((XmlSchemaSimpleContentExtension) t,
                        (XmlSchemaSimpleContentExtension) s));
        HANDLER_MAP.put(XmlSchemaSimpleContentRestriction.class::isInstance,
                (x, t, s) -> x.handleSimpleContentRestriction((XmlSchemaSimpleContentRestriction) t,
                        (XmlSchemaSimpleContentRestriction) s));
        HANDLER_MAP.put(XmlSchemaComplexType.class::isInstance,
                (x, t, s) -> x.handleComplexType((XmlSchemaComplexType) t, (XmlSchemaComplexType) s));
        HANDLER_MAP.put(XmlSchemaContentModel.class::isInstance,
                (x, t, s) -> x.handleContentModel((XmlSchemaContentModel) t, (XmlSchemaContentModel) s));
        HANDLER_MAP.put(XmlSchemaComplexContentExtension.class::isInstance,
                (x, t, s) -> x.handleComplexContentExtension((XmlSchemaComplexContentExtension) t,
                        (XmlSchemaComplexContentExtension) s));
        HANDLER_MAP.put(XmlSchemaComplexContentRestriction.class::isInstance,
                (x, t, s) -> x.handleComplexContentRestriction((XmlSchemaComplexContentRestriction) t,
                        (XmlSchemaComplexContentRestriction) s));
        HANDLER_MAP.put(XmlSchemaGroupParticle.class::isInstance,
                (x, t, s) -> x.handleGroupParticle((XmlSchemaGroupParticle) t, (XmlSchemaGroupParticle) s));
        HANDLER_MAP.put(XmlSchemaGroupRef.class::isInstance,
                (x, t, s) -> x.handleGroupRef((XmlSchemaGroupRef) t, (XmlSchemaGroupRef) s));
        HANDLER_MAP.put(XmlSchemaGroup.class::isInstance,
                (x, t, s) -> x.handleGroup((XmlSchemaGroup) t, (XmlSchemaGroup) s));
        HANDLER_MAP.put(XmlSchemaAny.class::isInstance, (x, t, s) -> {});
    }

    XmlSchemaExtractor(SchemaCollection targetSchemas, SchemaCollection sourceSchemas) {
        this.targetSchemas = targetSchemas;
        this.sourceSchemas = sourceSchemas;

        this.objectPairStack = new ArrayDeque<>();
        this.currentNamespace = new ArrayDeque<>();
    }

    public SchemaCollection getTargetSchemas() {
        return targetSchemas;
    }

    public SchemaCollection getSourceSchemas() {
        return sourceSchemas;
    }

    public XmlSchemaElement extract(XmlSchemaElement element) throws ParserException {

        // find target schema
        final XmlSchema targetSchema = getOrCreateTargetSchema(element.getQName().getNamespaceURI());

        // return if it's an existing top-level element, to avoid creating multiple copies
        final XmlSchemaElement result;
        final XmlSchemaElement existing = targetSchema.getElementByName(element.getQName());
        if (existing == null) {
            // create new topLevel element in target schema
            result = new XmlSchemaElement(targetSchema, true);
            result.setName(element.getName());

            // copy bean properties
            withNamespace(element.getQName().getNamespaceURI(), () -> copyXmlSchemaObjectBase(result, element));

        } else {
            result = existing;
        }

        return result;
    }

    public XmlSchemaElement extract(QName name, XmlSchemaType type) throws ParserException {

        // find target schema
        final XmlSchema targetSchema = getOrCreateTargetSchema(name.getNamespaceURI());

        // create new element in target schema
        final XmlSchemaElement result = new XmlSchemaElement(targetSchema, true);
        result.setName(name.getLocalPart());

        // set element type to the provided type
        final QName typeQName = type.getQName();
        withNamespace(typeQName.getNamespaceURI(), () -> setTargetTypeQName(typeQName, result::setSchemaTypeName));
        return result;
    }

    // gets existing target schema, or creates a new one in collection if missing
    public XmlSchema getOrCreateTargetSchema(String namespaceURI) {
        XmlSchema targetSchema = targetSchemas.getSchemaByTargetNamespace(namespaceURI);
        if (targetSchema == null) {
            targetSchema = targetSchemas.newXmlSchemaInCollection(namespaceURI);
            final NamespaceMap namespaceContext = new NamespaceMap();

            // add the usual suspects, for some reason these seem to be missing from the
            // generated SchemaSet
            namespaceContext.add("xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
            namespaceContext.add(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);

            targetSchema.setNamespaceContext(namespaceContext);

            final XmlSchema sourceSchema = sourceSchemas.getSchemaByTargetNamespace(namespaceURI);
            if (sourceSchema != null) {
                // copy schema properties from source schema
                targetSchema.setElementFormDefault(sourceSchema.getElementFormDefault());
                targetSchema.setAttributeFormDefault(sourceSchema.getAttributeFormDefault());
            }
        }

        return targetSchema;
    }

    /**
     * Copy all objects from source schema to target schema after calling extract methods to setup objects to extract.
     * @throws ParserException on error.
     */
    public void copyObjects() throws ParserException {
        while (!objectPairStack.isEmpty()) {

            // get objects in LIFO insertion order
            final ObjectPair<? extends XmlSchemaObjectBase> pair = objectPairStack.pop();
            final XmlSchemaObjectBase source = pair.getSource();
            final XmlSchemaObjectBase target = pair.getTarget();

            // namespace for new target objects created by pair handler
            final String targetNamespace;
            if (source instanceof XmlSchemaNamed) {
                targetNamespace = ((XmlSchemaNamed)source).getParent().getTargetNamespace();
            } else {
                targetNamespace = pair.getSourceSchemaNamespace();
            }

            // handle based on type
            withNamespace(targetNamespace, () ->
                HANDLER_MAP.entrySet().stream()
                        .filter(e -> e.getKey().test(source))
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElseThrow(() -> new ParserException("Unsupported type " + source.getClass().getName()))
                        .apply(this, target, source)
            );
        }

        // resolve targetSchemas imports
        targetSchemas.addCrossImports();
    }

    private void withNamespace(String namespace, ParseAction action) throws ParserException {
        final boolean saveOld = !namespace.equals(currentNamespace.peek());
        if (saveOld) {
            currentNamespace.push(namespace);
        }
        try {
            action.run();
        } finally {
            if (saveOld) {
                currentNamespace.pop();
            }
        }
    }

    private String getCurrentNamespace() {
        return currentNamespace.peek();
    }

    @FunctionalInterface
    private interface ParseAction {
        void run() throws ParserException;
    }

    private <T extends XmlSchemaObjectBase> void copyXmlSchemaObjectBase(T target, T source) throws ParserException {
        // copy non-blacklisted properties to target object
        copyNonBlackListedProperties(target, source);

        // put objects at top of stack (DFS) to be handled in copyObjects
        objectPairStack.push(new ObjectPair<>(target, source, getCurrentNamespace()));
    }

    private void handleItemWithRef(XmlSchemaItemWithRef<?> target, XmlSchemaItemWithRef<?> source) throws ParserException {
        final XmlSchemaNamed refTarget = source.getRef().getTarget();
        if (refTarget == null) {
            throw new ParserException("Missing ref target in source schemas: " + source.getRef().getTargetQName());
        }

        // add refTarget to targetSchemas
        createXmlSchemaObjectBase(refTarget);

        // TODO handle setting a ref name without a target object!!!
        target.getRef().setTargetQName(source.getTargetQName());
    }

    private void handleAttribute(XmlSchemaAttribute target, XmlSchemaAttribute source) throws ParserException {
        // set use if not top-level
        if (!source.isTopLevel()) {
            target.setUse(source.getUse());
        }
        handleTypeNameAndType(source.getSchemaTypeName(), source.getSchemaType(),
                target::setSchemaTypeName, target::setSchemaType);
    }

    private void handleElement(XmlSchemaElement target, XmlSchemaElement source) throws ParserException {
        // copy constraints as is
        target.getConstraints().addAll(source.getConstraints());

        // handle substitution group if present
        QName name = source.getSubstitutionGroup();
        // check if target schemas don't have the substitution element
        if (name != null && targetSchemas.getElementByQName(name) == null) {
            final XmlSchemaElement substitute = sourceSchemas.getElementByQName(name);
            if (substitute == null) {
                throw new ParserException("Missing element in source schemas: " + name);
            }

            // create a copy in target schemas
            createXmlSchemaObjectBase(substitute);
        }

        handleTypeNameAndType(source.getSchemaTypeName(), source.getSchemaType(),
            target::setSchemaTypeName, target::setType);
    }

    private void handleSimpleTypeList(XmlSchemaSimpleTypeList target, XmlSchemaSimpleTypeList source) throws ParserException {
        handleTypeNameAndType(source.getItemTypeName(), source.getItemType(),
                target::setItemTypeName, target::setItemType);
    }

    private void handleSimpleTypeRestriction(XmlSchemaSimpleTypeRestriction target,
                                             XmlSchemaSimpleTypeRestriction source) throws ParserException {
        target.getFacets().addAll(source.getFacets());
        handleTypeNameAndType(source.getBaseTypeName(), source.getBaseType(),
                target::setBaseTypeName, target::setBaseType);
    }

    private void handleSimpleTypeUnion(XmlSchemaSimpleTypeUnion target, XmlSchemaSimpleTypeUnion source) throws ParserException {

        final List<XmlSchemaSimpleType> targetBaseTypes = target.getBaseTypes();
        final List<QName> targetTypeNames = new ArrayList<>();

        // add base types
        for (XmlSchemaSimpleType baseType : source.getBaseTypes()) {
            targetBaseTypes.add(createXmlSchemaObjectBase(baseType));
        }

        // add member types by QName
        final QName[] typesQNames = source.getMemberTypesQNames();
        if (typesQNames != null) {
            for (QName name : typesQNames) {
                setTargetTypeQName(name, targetTypeNames::add);
            }
        }

        if (!targetTypeNames.isEmpty()) {
            // set target type names
            target.setMemberTypesQNames(targetTypeNames.toArray(new QName[0]));
        }
    }

    private void setTargetTypeQName(QName name, Consumer<QName> setType) throws ParserException {
        if (name != null) {
            // create target type if not already in target schema
            if (!isXSDSchemaType(name) && targetSchemas.getTypeByQName(name) == null) {
                createXmlSchemaObjectBase(getSourceTypeByQName(name));
            }
            setType.accept(name);
        }
    }

    private void handleSimpleType(XmlSchemaSimpleType target, XmlSchemaSimpleType source) throws ParserException {
        // handle content
        final XmlSchemaSimpleTypeContent sourceContent = source.getContent();
        if (sourceContent != null) {
            target.setContent(createXmlSchemaObjectBase(sourceContent));
        }
    }

    private void handleComplexType(XmlSchemaComplexType target, XmlSchemaComplexType source) throws ParserException {

        // copy attributes
        handleAttributesOrGroupRefs(target.getAttributes(), source.getAttributes());

        // handle contentModel
        final XmlSchemaContentModel sourceContentModel = source.getContentModel();
        if (sourceContentModel != null) {
            target.setContentModel(createXmlSchemaObjectBase(sourceContentModel));
        }

        handleParticle(source.getParticle(), target::setParticle);
    }

    // copy content to target schemas
    private void handleContentModel(XmlSchemaContentModel target, XmlSchemaContentModel source) throws ParserException {
        target.setContent(createXmlSchemaObjectBase(source.getContent()));
    }

    private void handleSimpleContentExtension(XmlSchemaSimpleContentExtension target,
                                              XmlSchemaSimpleContentExtension source) throws ParserException {
        handleAttributesOrGroupRefs(target.getAttributes(), source.getAttributes());

        // copy baseType if present
        setTargetTypeQName(source.getBaseTypeName(), target::setBaseTypeName);
    }

    private XmlSchemaType getSourceTypeByQName(QName typeName) throws ParserException {
        final XmlSchemaType sourceType = sourceSchemas.getTypeByQName(typeName);
        if (sourceType == null) {
            throw new ParserException("Missing type in source schemas: " + typeName);
        }

        return sourceType;
    }

    private void handleSimpleContentRestriction(XmlSchemaSimpleContentRestriction target,
                                                XmlSchemaSimpleContentRestriction source) throws ParserException {
        handleAttributesOrGroupRefs(target.getAttributes(), source.getAttributes());
        handleTypeNameAndType(source.getBaseTypeName(), source.getBaseType(), target::setBaseTypeName, target::setBaseType);
    }

    private void handleComplexContentRestriction(XmlSchemaComplexContentRestriction target,
                                                 XmlSchemaComplexContentRestriction source) throws ParserException {
        handleAttributesOrGroupRefs(target.getAttributes(), source.getAttributes());
        handleParticle(source.getParticle(), target::setParticle);

        // copy baseTypeName
        setTargetTypeQName(source.getBaseTypeName(), target::setBaseTypeName);
    }

    private void handleComplexContentExtension(XmlSchemaComplexContentExtension target,
                                               XmlSchemaComplexContentExtension source) throws ParserException {

        handleAttributesOrGroupRefs(target.getAttributes(), source.getAttributes());
        if (source.getParticle() != null) {
            handleParticle(source.getParticle(), target::setParticle);
        }
        setTargetTypeQName(source.getBaseTypeName(), target::setBaseTypeName);
    }

    // copy items from group particle to target
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void handleGroupParticle(XmlSchemaGroupParticle target, XmlSchemaGroupParticle source) throws ParserException {
        final List sourceItems;
        final List targetItems;

        // unfortunately the group 'all, choice and sequence' classes don't implement a common interface
        // hence the kludgy code below
        if (source instanceof XmlSchemaAll) {
            sourceItems = ((XmlSchemaAll) source).getItems();
            targetItems = ((XmlSchemaAll) target).getItems();
        } else if (source instanceof XmlSchemaChoice) {
            sourceItems = ((XmlSchemaChoice)source).getItems();
            targetItems = ((XmlSchemaChoice)target).getItems();
        } else if (source instanceof XmlSchemaSequence) {
            sourceItems = ((XmlSchemaSequence)source).getItems();
            targetItems = ((XmlSchemaSequence)target).getItems();
        } else {
            throw new ParserException("Unsupported Group Particle type " + source.getClass().getName());
        }

        // add all source items to target schemas
        for (Object item : sourceItems) {
            targetItems.add(createXmlSchemaObjectBase((XmlSchemaObjectBase) item));
        }
    }

    private void handleGroupRef(XmlSchemaGroupRef target, XmlSchemaGroupRef source) throws ParserException {
        // add group ref from QName
        final QName refName = source.getRefName();
        final XmlSchemaGroup group = sourceSchemas.getXmlSchemaCollection().getGroupByQName(refName);
        if (group == null) {
            throw new ParserException("Missing group in source schema: " + refName);
        }
        createXmlSchemaObjectBase(group);

        target.setRefName(refName);
    }

    private void handleGroup(XmlSchemaGroup target, XmlSchemaGroup source) throws ParserException {
        // copy group particle
        final XmlSchemaGroupParticle groupParticle = source.getParticle();
        handleParticle(groupParticle, target::setParticle);
    }

    // copy attributes and group refs
    private void handleAttributesOrGroupRefs(List<? super XmlSchemaAttributeOrGroupRef> target,
                                             List<? extends XmlSchemaAttributeOrGroupRef> source) throws ParserException {
        for (XmlSchemaAttributeOrGroupRef attribute : source) {

            if (attribute instanceof XmlSchemaAttributeGroupRef && ((XmlSchemaAttributeGroupRef) attribute).isRef()) {
                // add ref target group to target schemas
                final XmlSchemaAttributeGroup attributeGroup = ((XmlSchemaAttributeGroupRef) attribute).getRef().getTarget();
                createXmlSchemaObjectBase(attributeGroup);
            }

            target.add(createXmlSchemaObjectBase(attribute));
        }
    }

    private void handleAttributeGroup(XmlSchemaAttributeGroup target, XmlSchemaAttributeGroup source) throws ParserException {
        for (XmlSchemaAttributeGroupMember member : source.getAttributes()) {
            // add group member
            target.getAttributes().add((XmlSchemaAttributeGroupMember) createXmlSchemaObjectBase((XmlSchemaObjectBase)member));
        }
    }

    private <T extends XmlSchemaType> void handleTypeNameAndType(final QName typeName, final T type,
                                                                 final Consumer<QName> setTypeName,
                                                                 final Consumer<T> setType) throws ParserException {
        // set name if set
        setTargetTypeQName(typeName, setTypeName);
        // set type if set
        if (type != null) {
            setType.accept(createXmlSchemaObjectBase(type));
        }
    }

    private <T extends XmlSchemaParticle> void handleParticle(T particle, Consumer<T> setParticle) throws ParserException {
        if (particle != null) {
            setParticle.accept(createXmlSchemaObjectBase(particle));
        }
    }

    private static boolean isXSDSchemaType(QName typeName) {
        return Constants.URI_2001_SCHEMA_XSD.equals(typeName.getNamespaceURI());
    }

    @SuppressWarnings("unchecked")
    private <T extends XmlSchemaObjectBase> T createXmlSchemaObjectBase(T source) throws ParserException {

        // if source is an xsd:* type, return it as result
        if (source instanceof XmlSchemaType) {
            final QName qName = ((XmlSchemaType) source).getQName();
            if (qName != null && isXSDSchemaType(qName)) {
                return source;
            }
        }

        final String targetNamespace;
        final T target;

        try {
            // is it an XmlSchemaNamed that takes a schema as ctor argument?
            if (source instanceof XmlSchemaNamed) {

                final XmlSchemaNamed namedSource = (XmlSchemaNamed) source;

                // get target schema
                targetNamespace = namedSource.getParent().getTargetNamespace();
                final XmlSchema targetSchema = getOrCreateTargetSchema(targetNamespace);

                // if top-level, check if it already exists in targetSchema
                final boolean topLevel = namedSource.isTopLevel();
                if (topLevel) {
                    final Optional<XmlSchemaObject> targetObject = targetSchema.getItems().stream()
                        // find matching class and QName
                        .filter(i -> source.getClass().isInstance(i) && ((XmlSchemaNamed) i).getQName().equals(namedSource.getQName()))
                        .findFirst();

                    if (targetObject.isPresent()) {
                        // no need to create a new target object
                        return (T) targetObject.get();
                    }
                }
                target = (T) createXmlSchemaNamedObject(namedSource, targetSchema, topLevel);

            } else {
                // other XmlSchemaObject
                targetNamespace = getCurrentNamespace();
                target = createXmlSchemaObject(source, targetNamespace);
            }

        } catch (ReflectiveOperationException e) {
            throw new ParserException(String.format("Error extracting type %s: %s", source.getClass().getName(),
                    e.getMessage()), e);
        }

        // copy source to target using appropriate handlers
        withNamespace(targetNamespace, () -> copyXmlSchemaObjectBase(target,  source));

        return target;
    }

    private static XmlSchemaNamed createXmlSchemaNamedObject(XmlSchemaNamed source, XmlSchema targetSchema,
                                                                                boolean topLevel) throws ReflectiveOperationException, ParserException {
        // try getting a ctor with XmlSchema parameter and possibly a Boolean.TYPE
        final Class<? extends XmlSchemaNamed> sourceClass = source.getClass();
        final Constructor<?>[] constructors = sourceClass.getConstructors();
        if (constructors.length != 1) {
            throw new ParserException("Missing required constructor for named type " + source.getClass().getName());
        }

        // create target instance
        final XmlSchemaNamed target;
        if (constructors[0].getParameterCount() == 1) {
            target = (XmlSchemaNamed) constructors[0].newInstance(targetSchema);
        } else {
            target = (XmlSchemaNamed) constructors[0].newInstance(targetSchema, topLevel);
        }

        // copy the name if present
        final String name = source.getName();
        if (name != null) {
            target.setName(name);
            // copy the form if needed
            if (!topLevel && source instanceof XmlSchemaNamedWithForm) {
                ((XmlSchemaNamedWithForm) target).setForm(((XmlSchemaNamedWithForm) source).getForm());
            }
        }

        return target;
    }

    @SuppressWarnings("unchecked")
    private <T extends XmlSchemaObjectBase> T createXmlSchemaObject(T source, String targetNamespace) throws ReflectiveOperationException {
        T target;
        final Constructor<?> constructor = source.getClass().getConstructors()[0];
        if (constructor.getParameterCount() == 0) {
            target = (T) constructor.newInstance();
        } else {
            // some ref types have a constructor that takes an XmlSchema,
            final XmlSchema targetSchema = targetSchemas.getSchemaByTargetNamespace(targetNamespace);
            target = (T) constructor.newInstance(targetSchema);
        }
        return target;
    }

    private static <T extends XmlSchemaObjectBase> void copyNonBlackListedProperties(T target, T source) throws ParserException {
        // copy all properties excluding black listed properties
        try {
            final List<String> properties = BLACK_LISTED_PROPERTIES_MAP.get(source.getClass());
            if (properties == null) {
                BeanUtils.copyProperties(target, source);
            } else {
                // iterate through properties and copy ones not in the black list
                for (PropertyDescriptor origDescriptor : PropertyUtils.getPropertyDescriptors(source)) {
                    final String name = origDescriptor.getName();
                    // ignore 'class' and blacklisted properties
                    if ("class".equals(name) || properties.contains(name)) {
                        continue;
                    }
                    if (PropertyUtils.isReadable(source, name) && PropertyUtils.isWriteable(target, name)) {
                        try {
                            final Object value = PropertyUtils.getSimpleProperty(source, name);
                            BeanUtils.copyProperty(target, name, value);
                        } catch (final NoSuchMethodException e) {
                            throw new ParserException(String.format("Unexpected missing method exception: %s",
                                e.getMessage()), e);
                        }
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ParserException(String.format("Error creating type %s: %s", target.getClass().getName(),
                    e.getMessage()), e);
        }
    }

    @FunctionalInterface
    private interface TriConsumer<X, T, S> {
        void apply(X x, T t, S s) throws ParserException;
    }

    private static class ObjectPair<T extends XmlSchemaObjectBase> {

        private final T target;
        private final T source;
        private final String sourceSchemaNamespace;

        ObjectPair(T target, T source, String sourceSchemaNamespace) {
            this.target = target;
            this.source = source;
            this.sourceSchemaNamespace = sourceSchemaNamespace;
        }

        public T getTarget() {
            return target;
        }

        public T getSource() {
            return source;
        }

        public String getSourceSchemaNamespace() {
            return sourceSchemaNamespace;
        }
    }
}
