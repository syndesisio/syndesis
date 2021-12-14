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
package io.syndesis.connector.meta.v1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.syndesis.common.util.SyndesisServerException;

@Component
@Path("/drivers")
public class DriverUploadEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(DriverUploadEndpoint.class);
    public static final String EXT_DIR = System.getProperty("LOADER_HOME", "/deployments/ext");

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Boolean upload(MultipartFormDataInput input) {

        String fileName = getFileName(input);
        storeFile(extensionJarPath(fileName), input);

        LOGGER.info("Driver {} succefully uploaded", fileName);
        return Boolean.TRUE;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Boolean delete(@PathParam("id") String safeExtensionId) {

        Boolean isDeleted = Boolean.FALSE;
        File extensionFile = extensionJarPath(safeExtensionId);

        if (extensionFile.exists()) {
            isDeleted = extensionFile.delete();

            if (isDeleted) {
                LOGGER.info("Extension {} succesfully deleted", safeExtensionId);
            } else {
                LOGGER.error("Extension {} could not be deleted", safeExtensionId);
            }
        } else {
            LOGGER.warn("Extension {} does not exist", safeExtensionId);
        }

        return isDeleted;
    }

    private static File extensionJarPath(final String given) {
        final String name = new File(given + ".jar").getName();

        return new File(EXT_DIR, name);
    }

    private static void storeFile(File file, MultipartFormDataInput dataInput) {
        // Store the artifact into /deployments/ext
        try (InputStream is = getBinaryArtifact(dataInput)) {
            Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw SyndesisServerException.launderThrowable("Unable to store the driver file into " + EXT_DIR, ex);
        }
    }

    private static String getFileName(MultipartFormDataInput input) {
        if (input == null || input.getParts() == null || input.getParts().isEmpty()) {
            throw new IllegalArgumentException("Multipart request is empty");
        }
        try {
            final String fileName = input.getFormDataPart("fileName", String.class, null);
            if (fileName == null) {
                throw new IllegalArgumentException("Can't find a valid 'fileName' part in the multipart request");
            }
            return fileName;
        } catch (IOException ex) {
            throw SyndesisServerException.launderThrowable("Unable to obtain fileName", ex);
        }
    }

    private static InputStream getBinaryArtifact(MultipartFormDataInput input) {
        if (input == null || input.getParts() == null || input.getParts().isEmpty()) {
            throw new IllegalArgumentException("Multipart request is empty");
        }

        try {
            final InputStream result = input.getFormDataPart("file", InputStream.class, null);

            if (result == null) {
                throw new IllegalArgumentException("Can't find a valid 'file' part in the multipart request");
            }

            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error while reading multipart request", e);
        }
    }

}
