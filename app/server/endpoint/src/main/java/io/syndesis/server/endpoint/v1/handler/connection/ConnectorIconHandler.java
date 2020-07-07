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
package io.syndesis.server.endpoint.v1.handler.connection;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.dao.file.FileDataManager;
import io.syndesis.server.dao.file.IconDao;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.icon.Icon;
import io.syndesis.server.endpoint.v1.handler.BaseHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Tag(name = "connector")
@Tag(name = "connector-icon")
public final class ConnectorIconHandler extends BaseHandler {

    private final Connector connector;
    private final IconDao iconDao;
    private final FileDataManager extensionDataManager;

    ConnectorIconHandler(final DataManager dataMgr, final Connector connector, final IconDao iconDao,
                                       final FileDataManager extensionDataManager) {
        super(dataMgr);
        this.connector = connector;
        this.iconDao = iconDao;
        this.extensionDataManager = extensionDataManager;
    }

    @POST
    @Operation(description = "Updates the connector icon for the specified connector and returns the updated connector")
    @ApiResponse(responseCode = "200", description = "Updated Connector icon")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @SuppressFBWarnings
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public Connector create(MultipartFormDataInput dataInput) {
        if (dataInput == null || dataInput.getParts() == null || dataInput.getParts().isEmpty()) {
            throw new IllegalArgumentException("Multipart request is empty");
        }

        if (dataInput.getParts().size() != 1) {
            throw new IllegalArgumentException("Wrong number of parts in multipart request");
        }

        try {
            InputPart filePart = dataInput.getParts().iterator().next();
            try (InputStream result = filePart.getBody(InputStream.class, null)) {

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
                    //write icon to (Sql)FileStore
                    iconDao.write(icon.getId().get(), iconStream);

                    Connector updatedConnector = new Connector.Builder().createFrom(connector).icon("db:" + icon.getId().get()).build();
                    getDataManager().update(updatedConnector);
                    return updatedConnector;
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error while reading multipart request", e);
        }
    }

    @GET
    @SuppressWarnings("PMD.CyclomaticComplexity")
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
            //grab icon file from the (Sql)FileStore
            final StreamingOutput streamingOutput = (out) -> {
                try (BufferedSink sink = Okio.buffer(Okio.sink(out));
                    Source source = Okio.source(iconDao.read(connectorIconId))) {
                    sink.writeAll(source);
                }
            };
            return Response.ok(streamingOutput, icon.getMediaType()).build();
        } else if (connectorIcon.startsWith("extension:")) {
            String iconFile = connectorIcon.substring(10);
            Optional<InputStream> extensionIcon = connector.getDependencies().stream()
                .filter(Dependency::isExtension)
                .map(Dependency::getId)
                .findFirst()
                .flatMap(extensionId -> extensionDataManager.getExtensionIcon(extensionId, iconFile));

            if (extensionIcon.isPresent()) {
                final StreamingOutput streamingOutput = (out) -> {
                    try (BufferedSink sink = Okio.buffer(Okio.sink(out)); InputStream iconStream = extensionIcon.get()) {
                        sink.writeAll(Okio.source(iconStream));
                    }
                };
                return Response.ok(streamingOutput, extensionDataManager.getExtensionIconMediaType(iconFile)).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
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
            if (!StringUtils.isEmpty(contentLength)) {
                actualResponse.header(CONTENT_LENGTH, contentLength);
            }

            return actualResponse.build();
        } catch (final IOException e) {
            throw new SyndesisServerException(e);
        }
    }
}
