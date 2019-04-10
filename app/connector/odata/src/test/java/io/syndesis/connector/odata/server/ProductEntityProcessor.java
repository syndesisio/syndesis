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
import java.util.List;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductEntityProcessor implements EntityProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ODataTestServer.class);

    private OData odata;
	private ServiceMetadata serviceMetadata;
	private Storage storage;

	public ProductEntityProcessor(Storage storage) {
		this.storage = storage;
	}

	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	@Override
	public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
							throws ODataApplicationException, SerializerException {

		// 1. retrieve the Entity Type
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		// Note: only in our example we can assume that the first segment is the EntitySet
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

		// 2. retrieve the data from backend
		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		Entity entity = storage.readEntityData(edmEntitySet, keyPredicates);
		LOG.info("Reading entity {}", entity.getSelfLink());

		// 3. serialize
		EdmEntityType entityType = edmEntitySet.getEntityType();

		ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).suffix(ContextURL.Suffix.ENTITY).build();
	 	// expand and select currently not supported
		EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

		ODataSerializer serializer = this.odata.createSerializer(responseFormat);
		SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType, entity, options);
		InputStream entityStream = serializerResult.getContent();

		//4. configure the response object
		response.setContent(entityStream);
		response.setStatusCode(HttpStatusCode.OK.getStatusCode());
		response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}




	/*
	 * These processor methods are not handled in this tutorial
	 * */

	@Override
	public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)
				throws ODataApplicationException, DeserializerException, SerializerException {
	    // 1. Retrieve the entity type from the URI
	    EdmEntitySet edmEntitySet = Util.getEdmEntitySet(uriInfo);
	    EdmEntityType edmEntityType = edmEntitySet.getEntityType();

	    // 2. create the data in backend
	    // 2.1. retrieve the payload from the POST request for the entity to create and deserialize it
	    InputStream requestInputStream = request.getBody();
	    ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
	    DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
	    Entity requestEntity = result.getEntity();
	    LOG.info("Creating new entity {}", requestEntity);

	    // 2.2 do the creation in backend, which returns the newly created entity
	    Entity createdEntity = storage.createEntityData(edmEntitySet, requestEntity);

	    // 3. serialize the response (we have to return the created entity)
	    ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
	      // expand and select currently not supported
	    EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

	    ODataSerializer serializer = this.odata.createSerializer(responseFormat);
	    SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, options);

	    //4. configure the response object
	    response.setContent(serializedResponse.getContent());
	    response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
	    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	@Override
	public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)
							throws ODataApplicationException, DeserializerException, SerializerException {
	    // 1. Retrieve the entity set which belongs to the requested entity
	    List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
	    // Note: only in our example we can assume that the first segment is the EntitySet
	    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
	    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
	    EdmEntityType edmEntityType = edmEntitySet.getEntityType();

	    // 2. update the data in backend
	    // 2.1. retrieve the payload from the PUT request for the entity to be updated
	    InputStream requestInputStream = request.getBody();
	    ODataDeserializer deserializer = this.odata.createDeserializer(requestFormat);
	    DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
	    Entity requestEntity = result.getEntity();
	    LOG.info("Updating entity {}", requestEntity.getSelfLink());

	    // 2.2 do the modification in backend
	    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
	    // Note that this updateEntity()-method is invoked for both PUT or PATCH operations
	    HttpMethod httpMethod = request.getMethod();
	    storage.updateEntityData(edmEntitySet, keyPredicates, requestEntity, httpMethod);

	    //3. configure the response object
	    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}

	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException {
	 // 1. Retrieve the entity set which belongs to the requested entity
	    List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
	    // Note: only in our example we can assume that the first segment is the EntitySet
	    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
	    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

	    // 2. delete the data in backend
	    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
	    for (UriParameter kp : keyPredicates) {
	        LOG.info("Deleting entity {} ( {} )", kp.getName(), kp.getText());
	    }

	    storage.deleteEntityData(edmEntitySet, keyPredicates);

	    //3. configure the response object
	    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
	}

}
