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
package io.syndesis.rest.v1.handler.connection;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.dao.icon.IconDataAccessObject;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.integration.support.Strings;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.icon.Icon;
import io.syndesis.rest.v1.handler.BaseHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.BufferedSink;
import okio.Okio;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

@Api(tags = {"connector", "connector-icon"})
public final class ConnectorIconHandler extends BaseHandler {

    private final Connector connector;
    private final IconDataAccessObject iconDao;

    /* default */ ConnectorIconHandler(final DataManager dataMgr, final Connector connector, final IconDataAccessObject iconDao) {
        super(dataMgr);
        this.connector = connector;
        this.iconDao = iconDao;
    }

    @POST
    @ApiOperation("Updates the connector icon for the specified connector and returns the updated connector")
    @ApiResponses(@ApiResponse(code = 200, response = Connector.class, message = "Updated Connector icon"))
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Connector create(MultipartFormDataInput dataInput) {
        if (dataInput == null || dataInput.getParts() == null || dataInput.getParts().isEmpty()) {
            throw new IllegalArgumentException("Multipart request is empty");
        }

        if (dataInput.getParts().size() != 1) {
            throw new IllegalArgumentException("Wrong number of parts in multipart request");
        }

        try {
            InputPart filePart = dataInput.getParts().iterator().next();
            InputStream result = filePart.getBody(InputStream.class, null);

            if (result == null) {
                throw new IllegalArgumentException("Can't find a valid 'icon' part in the multipart request");
            }

            try (BufferedInputStream iconStream = new BufferedInputStream(result)) {
                MediaType mediaType = filePart.getMediaType();
                if (!mediaType.getType().equals("image")) {
                    // URLConnection.guessContentTypeFromStream resets the stream after inspecting the media type so
                    // can continue to be used, rather than being consumed.
                    String guessedMediaType = URLConnection.guessContentTypeFromStream(iconStream);
                    if (!guessedMediaType.startsWith("image/")) {
                        throw new IllegalArgumentException("Invalid file contents for an image");
                    }
                    mediaType = MediaType.valueOf(guessedMediaType);
                }

                Icon.Builder iconBuilder = new Icon.Builder()
                    .mediaType(mediaType.toString());

                Icon icon;
                String connectorIcon = connector.getIcon();
                if (connectorIcon != null && connectorIcon.startsWith("db:")) {
                    String connectorIconId = connectorIcon.substring(3);
                    iconBuilder.id(connectorIconId);
                    icon = iconBuilder.build();
                    getDataManager().update(icon);
                } else {
                    icon = getDataManager().create(iconBuilder.build());
                }

                iconDao.write(icon.getId().get(), iconStream);

                Connector updatedConnector = new Connector.Builder().createFrom(connector).icon("db:" + icon.getId().get()).build();
                getDataManager().update(updatedConnector);
                return updatedConnector;
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error while reading multipart request", e);
        }
    }

    @GET
    public Response get() {
        String connectorIcon = connector.getIcon();
        if (connectorIcon == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (connectorIcon.startsWith("db:")) {
            String connectorIconId = connectorIcon.substring(3);
            Icon icon = getDataManager().fetch(Icon.class, connectorIconId);
            if (icon == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            final StreamingOutput streamingOutput = (out) -> {
                final BufferedSink sink = Okio.buffer(Okio.sink(out));
                sink.writeAll(Okio.source(iconDao.read(connectorIconId)));
                sink.close();
            };
            return Response.ok(streamingOutput, icon.getMediaType()).build();
        }

        // If the specified icon is a data URL, or a non-URL like value (e.g.
        // font awesome class name), return 404
        if (connectorIcon.startsWith("data:") || !connectorIcon.contains("/")) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final OkHttpClient httpClient = new OkHttpClient();
        try {
            final okhttp3.Response externalResponse = httpClient.newCall(new Request.Builder().get().url(connectorIcon).build()).execute();
            final String contentType = externalResponse.header(CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
            final String contentLength = externalResponse.header(CONTENT_LENGTH);

            final StreamingOutput streamingOutput = (out) -> {
                final BufferedSink sink = Okio.buffer(Okio.sink(out));
                sink.writeAll(externalResponse.body().source());
                sink.close();
            };

            final Response.ResponseBuilder actualResponse = Response.ok(streamingOutput, contentType);
            if (!Strings.isEmpty(contentLength)) {
                actualResponse.header(CONTENT_LENGTH, contentLength);
            }

            return actualResponse.build();
        } catch (final IOException e) {
            throw new SyndesisServerException(e);
        }
    }
}
