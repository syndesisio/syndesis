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

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.syndesis.common.util.SyndesisServerException;

@Component
@Path("/drivers")
public class DriverUploadEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(DriverUploadEndpoint.class);
    public static final String EXT_DIR = System.getProperty("LOADER_PATH", "/deployments/ext");

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Boolean upload(MultipartFormDataInput input) {

        String fileName = getFileName(input);
        storeFile(String.format("%s/%s", EXT_DIR, fileName), input);

        LOGGER.info("Driver %s succefully uploaded", fileName);
        return Boolean.TRUE;
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Boolean delete(@PathParam("id") String safeExtensionId) {

        Boolean isDeleted = Boolean.FALSE;
        File extensionDir = new File(String.format("%s/%s", EXT_DIR, safeExtensionId));

        if (extensionDir.exists()) {
            try {

                workaroundDeleteDriverJar(extensionDir);

                FileUtils.deleteDirectory(extensionDir);
                LOGGER.info("Extension %s succesfully deleted", safeExtensionId);
                isDeleted = Boolean.TRUE;

            } catch (IOException e) {
                LOGGER.error("Extension %s could not be deleted", safeExtensionId, e);
            }

        } else {
            LOGGER.error("Extension %s does not exist", safeExtensionId);
        }

        return isDeleted;
    }

    private void storeFile(String location, MultipartFormDataInput dataInput) {
        // Store the artifact into /deployments/ext
        try (final InputStream is = getBinaryArtifact(dataInput)) {
            final File file = File.createTempFile("ext", "jar");

            Files.copy(
                    is,
                    file.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            closeQuietly(is);
            extractJar(file, location);
            if (! file.delete()) {
                LOGGER.warn("Could not delete file %s ", file.getPath());
            }

        } catch (IOException ex) {
            throw SyndesisServerException.launderThrowable("Unable to store the driver file into " + EXT_DIR, ex);
        }
    }

    private void extractJar(File jar, String dest) throws IOException {
        JarFile jarFile = new JarFile(jar);
        final File destDir = new File(dest);
        boolean isCreated = destDir.mkdir();
        if (isCreated) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                File file = new File(dest + File.separator + entry.getName());
                if (entry.isDirectory()) {
                    if (file.mkdir()) {
                        LOGGER.warn("Dir %s already exists", file.getPath());
                    }
                    continue;
                }
                InputStream is = null;
                FileOutputStream os = null;
                try {
                    is = jarFile.getInputStream(entry);
                    os = new FileOutputStream(file);
                    while (is.available() > 0) {
                        os.write(is.read());
                    }
                } finally {
                    closeQuietly(os);
                    closeQuietly(is);
                }

                //Workaround
                if (file.getPath().startsWith(dest + File.separator + "lib")) {
                    FileUtils.copyFileToDirectory(file, new File(EXT_DIR));
                }
            }
        } else {
            LOGGER.warn("Dir %s already existed.", dest);
        }
        jarFile.close();
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    private void closeQuietly(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    private String getFileName(MultipartFormDataInput input) {
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

    private InputStream getBinaryArtifact(MultipartFormDataInput input) {
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

    //For now as workaround delete driver jars found from EXT_DIR
    private void workaroundDeleteDriverJar(File extensionDir) {
        File libDir = new File(extensionDir.getAbsolutePath() + File.separator + "lib");
        if (libDir != null && libDir.exists() && libDir.isDirectory()) {
            File[] files = libDir.listFiles();
            if (files != null) {
                for (final File fileEntry : files) {
                    File driverJar = new File(EXT_DIR + File.separator + fileEntry.getName());
                    if (driverJar.exists() && !driverJar.delete()) {
                        LOGGER.warn("Could not delete jar %s", driverJar.getPath());
                    }
                }
            }
        }
    }
}
