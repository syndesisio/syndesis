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
package io.syndesis.connector.odata.server;

import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

/**
 * Taken from the tutorial:
 * https://olingo.apache.org/doc/odata4/tutorials/read/tutorial_read.html
 */
public class ProductsEntityCollectionProcessor implements EntityCollectionProcessor {

    private OData odata;
    private ServiceMetadata serviceMetadata;
    private Storage storage;

    public ProductsEntityCollectionProcessor(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
        throws ODataApplicationException, ODataLibraryException {
        // 1st we have retrieve the requested EntitySet from the uriInfo object (representation of the parsed service URI)
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet)resourcePaths.get(0); // in our example, the first segment is the EntitySet
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        // 2nd: fetch the data from backend for this requested EntitySetName
        // it has to be delivered as EntitySet object
        EntityCollection entityCollection = storage.readEntitySetData(edmEntitySet);

        // 3rd: apply System Query Options
        // modify the result set according to the query options, specified by the end user
        List<Entity> entityList = entityCollection.getEntities();
        EntityCollection returnEntityCollection = new EntityCollection();

        // handle $count: always return the original number of entities, without considering $top and $skip
        CountOption countOption = uriInfo.getCountOption();
        if (countOption != null) {
            boolean isCount = countOption.getValue();
            if (isCount) {
                returnEntityCollection.setCount(entityList.size());
            }
        }

        applyQueryOptions(uriInfo, entityList, returnEntityCollection);

        // 3rd: create a serializer based on the requested format (json)
        ODataSerializer serializer = odata.createSerializer(responseFormat);

        // 4th: Now serialize the content: transform from the EntitySet object to InputStream
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
        ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();

        final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
        EntityCollectionSerializerOptions opts =
            EntityCollectionSerializerOptions
                .with().id(id)
                .count(countOption)
                .contextURL(contextUrl)
                .build();

        SerializerResult serializerResult = serializer.entityCollection(serviceMetadata,
                                                                        edmEntityType,
                                                                        returnEntityCollection,
                                                                        opts);
        InputStream serializedContent = serializerResult.getContent();

        // Finally: configure the response object: set the body, headers and status code
        response.setContent(serializedContent);
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    private EntityCollection applyQueryOptions(UriInfo uriInfo, List<Entity> entityList, EntityCollection returnEntityCollection) throws ODataApplicationException {
        // handle $skip
        SkipOption skipOption = uriInfo.getSkipOption();
        if (skipOption != null) {
            int skipNumber = skipOption.getValue();
            if (skipNumber >= 0) {
                if (skipNumber <= entityList.size()) {
                    entityList = entityList.subList(skipNumber, entityList.size());
                } else {
                    // The client skipped all entities
                    entityList.clear();
                }
            } else {
                throw new ODataApplicationException("Invalid value for $skip", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                    Locale.ROOT);
            }
        }

        // handle $top
        TopOption topOption = uriInfo.getTopOption();
        if (topOption != null) {
            int topNumber = topOption.getValue();
            if (topNumber >= 0) {
                if (topNumber <= entityList.size()) {
                    entityList = entityList.subList(0, topNumber);
                } // else the client has requested more entities than available => return what we have
            } else {
                throw new ODataApplicationException("Invalid value for $top", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                                                    Locale.ROOT);
            }
        }

        // handle $filter
        FilterOption filterOption = uriInfo.getFilterOption();
        if(filterOption != null) {
            // Apply $filter system query option
            try {
                  Iterator<Entity> entityIterator = entityList.iterator();

                  // Evaluate the expression for each entity
                  // If the expression is evaluated to "true", keep the entity otherwise remove it from the entityList
                  while (entityIterator.hasNext()) {
                      // To evaluate the the expression, create an instance of the Filter Expression Visitor and pass
                      // the current entity to the constructor
                      Entity currentEntity = entityIterator.next();
                      Expression filterExpression = filterOption.getExpression();
                      FilterExpressionVisitor expressionVisitor = new FilterExpressionVisitor(currentEntity);

                      // Start evaluating the expression
                      Object visitorResult = filterExpression.accept(expressionVisitor);

                      // The result of the filter expression must be of type Edm.Boolean
                      if(visitorResult instanceof Boolean) {
                          if(!Boolean.TRUE.equals(visitorResult)) {
                            // The expression evaluated to false (or null), so we have to remove the currentEntity from entityList
                            entityIterator.remove();
                          }
                      } else {
                          throw new ODataApplicationException("A filter expression must evaulate to type Edm.Boolean",
                              HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
                      }
                  }

                } catch (ExpressionVisitException e) {
                  throw new ODataApplicationException("Exception in filter evaluation",
                      HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
                }
        }

        // after applying the system query options, create the EntityCollection based on the reduced list
        for (Entity entity : entityList) {
            returnEntityCollection.getEntities().add(entity);
        }

        // apply $orderby
        applyOrderby(uriInfo, returnEntityCollection);

        return returnEntityCollection;
    }

    private void applyOrderby(UriInfo uriInfo, EntityCollection entityCollection) {
        List<Entity> entityList = entityCollection.getEntities();

        OrderByOption orderByOption = uriInfo.getOrderByOption();
        if (orderByOption == null) {
            return;
        }

        List<OrderByItem> orderItemList = orderByOption.getOrders();
        OrderByItem orderByItem = orderItemList.get(0); // in our example we support only one
        Expression expression = orderByItem.getExpression();
        if (! (expression instanceof Member)) {
            return;
        }

        UriInfoResource resourcePath = ((Member)expression).getResourcePath();
        UriResource uriResource = resourcePath.getUriResourceParts().get(0);
        if (! (uriResource instanceof UriResourcePrimitiveProperty)) {
            return;
        }

        EdmProperty edmProperty = ((UriResourcePrimitiveProperty)uriResource).getProperty();
        String sortPropertyName = edmProperty.getName();

        // do the sorting for the list of entities
        Collections.sort(entityList, new Comparator<Entity>() {

            // we delegate the sorting to the native sorter of Integer and String
            @Override
            public int compare(Entity entity1, Entity entity2) {
                int compareResult = 0;

                if (sortPropertyName.equals("ID")) {
                    Integer integer1 = (Integer)entity1.getProperty(sortPropertyName).getValue();
                    Integer integer2 = (Integer)entity2.getProperty(sortPropertyName).getValue();
                    compareResult = integer1.compareTo(integer2);
                } else {
                    String propertyValue1 = (String)entity1.getProperty(sortPropertyName).getValue();
                    String propertyValue2 = (String)entity2.getProperty(sortPropertyName).getValue();
                    compareResult = propertyValue1.compareTo(propertyValue2);
                }

                // if 'desc' is specified in the URI, change the order of the list
                if (orderByItem.isDescending()) {
                    return -compareResult; // just convert the result to negative value to change the order
                }

                return compareResult;
            }
        });
    }
}
