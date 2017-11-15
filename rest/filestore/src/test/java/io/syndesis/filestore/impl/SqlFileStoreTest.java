/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.filestore.impl;

import io.syndesis.filestore.FileStoreException;
import org.apache.commons.io.IOUtils;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.skife.jdbi.v2.DBI;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class SqlFileStoreTest {

    private SqlFileStore fileStore;

    @Parameterized.Parameters
    public static Collection<Object[]> configs() {
        EmbeddedDataSource derbyDs = new EmbeddedDataSource();
        derbyDs.setDatabaseName("test");
        derbyDs.setCreateDatabase("create");

        JdbcDataSource h2Ds = new JdbcDataSource();
        h2Ds.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");

        // Tests on postgres disabled

//        PGPoolingDataSource postgresDs = new PGPoolingDataSource();
//        postgresDs.setServerName("localhost");
//        postgresDs.setDatabaseName("syndesis");
//        postgresDs.setUser("postgres");
//        postgresDs.setPassword("password");

        return Arrays.asList(new Object[][]{
            {derbyDs},
//            {postgresDs},
            {h2Ds}
        });
    }

    public SqlFileStoreTest(DataSource ds) throws Exception {
        DBI dbi = new DBI(ds);
        this.fileStore = new SqlFileStore(dbi);
        this.fileStore.destroy();
        this.fileStore.init();
    }

    @Test
    public void testSmallFile() throws IOException {
        String path = "/file";
        String content = "Hello Wòrld!";

        write(path, content.getBytes(StandardCharsets.UTF_8));
        String retrieved = read(path, StandardCharsets.UTF_8);

        assertEquals(content, retrieved);

        fileStore.delete(path);
        assertNull(read(path));
    }

    @Test
    public void testBiggerFile() throws IOException {
        String path = "/bigfile";
        byte[] data = new byte[4_000_000];
        data[2017] = 42;

        write(path, data);
        byte[] retrieved = read(path);
        assertArrayEquals(data, retrieved);

        fileStore.delete(path);
        assertNull(read(path));
    }

    @Test
    public void testOverwrite() throws IOException {
        String path = "/file";
        String anotherPath = "/dir/file";
        String content1 = "Hello Wòrld!";
        String content2 = "Hello Wòrld2!";

        write(anotherPath, content1.getBytes(StandardCharsets.UTF_8));
        assertEquals(content1, read(anotherPath, StandardCharsets.UTF_8));

        write(path, content1.getBytes(StandardCharsets.UTF_8));
        assertEquals(content1, read(path, StandardCharsets.UTF_8));

        write(path, content2.getBytes(StandardCharsets.UTF_8));
        assertEquals(content2, read(path, StandardCharsets.UTF_8));

        assertEquals(content1, read(anotherPath, StandardCharsets.UTF_8));

        fileStore.delete(path);
        assertNull(read(path));

        assertEquals(content1, read(anotherPath, StandardCharsets.UTF_8));
        fileStore.delete(anotherPath);
        assertNull(read(anotherPath));
    }

    @Test
    public void testMove() throws IOException {
        String content = "Hello Wòrld!";

        write("/file1", content.getBytes(StandardCharsets.UTF_8));
        assertTrue(fileStore.move("/file1", "/file2"));
        assertEquals(content, read("/file2", StandardCharsets.UTF_8));
        assertNull(read("/file1"));
    }

    @Test
    public void testMoveOverwrite() throws IOException {
        String content1 = "Hello Wòrld!";
        String content2 = "Hello Wòrld2!";

        write("/file1", content1.getBytes(StandardCharsets.UTF_8));
        write("/file2", content2.getBytes(StandardCharsets.UTF_8));
        assertTrue(fileStore.move("/file1", "/file2"));
        assertEquals(content1, read("/file2", StandardCharsets.UTF_8));
        assertNull(read("/file1"));
    }

    @Test
    public void testWrongMove() throws IOException {
        String content = "Hello Wòrld!";

        write("/file", content.getBytes(StandardCharsets.UTF_8));
        assertFalse(fileStore.move("/this-path-does-not-exist", "/file"));
        assertEquals(content, read("/file", StandardCharsets.UTF_8));
        assertNull(read("/this-path-does-not-exist"));
    }

    @Test
    public void testMoveTempFile() throws IOException {
        String content = "Hello Wòrld!";
        String temp = writeTemp(content.getBytes(StandardCharsets.UTF_8));
        FileStoreSupport.checkValidPath(temp);
        fileStore.move(temp, "/home/file");
        assertEquals(content, read("/home/file", StandardCharsets.UTF_8));
        assertNull(read(temp));
    }

    @Test
    public void testAllMethodsRefuseInvalidTokensInPath() throws IOException {
        byte[] dummyContent = "Hello".getBytes(StandardCharsets.UTF_8);
        String[] invalidTokens = {"/", "\\", "%", "$", "#", "\n", "\r", "\"", "'", "!"};
        for (String token : invalidTokens) {
            expectInvalidPath(() -> {
                write("/dir" + token, dummyContent);
                Assert.fail("Should throw exception");
                return true;
            });

            expectInvalidPath(() -> read("/dir" + token));
            expectInvalidPath(() -> fileStore.delete("/dir" + token));
        }
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void testAllowedPaths() throws IOException {
        // Allowed
        read("/connector_2.11-2.15-SNAPSHOT.jar");
        read("/path/to/connector_2.11-2.15-SNAPSHOT.jar");
        read("/path/to/file.");
        read("/a/b/cccccc---------");
        read("/.hidden/path/to/connector_2.11-2.15-SNAPSHOT.jar");
        read("/.hidden/path/.to/connector_2.11-2.15-SNAPSHOT.jar");
        read("/.hidden/path/.to/.connector_2.11-2.15-SNAPSHOT.jar");
        read("/hello world/to/File (1).zip");

        // Not allowed
        expectInvalidPath(() -> fileStore.read("/"));
        expectInvalidPath(() -> fileStore.read("//a"));
        expectInvalidPath(() -> fileStore.read("/a/a/"));
        expectInvalidPath(() -> fileStore.read("a"));
        expectInvalidPath(() -> fileStore.read("/b/c/d--/"));
        expectInvalidPath(() -> fileStore.read("\\a"));
        expectInvalidPath(() -> fileStore.read("/è"));
        expectInvalidPath(() -> fileStore.read("/#"));
        expectInvalidPath(() -> fileStore.read("/a/b/c//aa.jar"));
        expectInvalidPath(() -> fileStore.read("/."));
        expectInvalidPath(() -> fileStore.read("/a/."));
        expectInvalidPath(() -> fileStore.read("/a/b/."));
        expectInvalidPath(() -> fileStore.read("/a/b/./c"));
        expectInvalidPath(() -> fileStore.read("/ a"));
        expectInvalidPath(() -> fileStore.read("/ a/b"));
        expectInvalidPath(() -> fileStore.read("/a/ b"));
        expectInvalidPath(() -> fileStore.read("/ /b"));
        expectInvalidPath(() -> fileStore.read("/a/b "));
        expectInvalidPath(() -> fileStore.read("/a /b "));
    }

    @Test
    public void testNoLeak() throws IOException {
        byte[] dummyContent = "Hello".getBytes(StandardCharsets.UTF_8);
        for (int i=0; i<100; i++) {
            write("/file" + i, dummyContent);
            assertArrayEquals(dummyContent, read("/file" + i));
            fileStore.delete("/file" + i);
        }
    }

    @Test
    public void testMultipleInitAreIdempotent() throws IOException {
        fileStore.init();
        fileStore.init();
        fileStore.init();
        fileStore.init();
        assertNull(fileStore.read("/not-exists"));
    }

    @Test
    public void testMultipleDestroyAreIdempotent() throws IOException {
        fileStore.destroy();
        fileStore.destroy();
        fileStore.destroy();
        fileStore.destroy();
        fileStore.init();
        assertNull(fileStore.read("/not-exists"));
    }

    private <T> void expectInvalidPath(Callable<T> callable) {
        assertNotNull(callable);
        try {
            callable.call();
            Assert.fail("Expected exception");
        } catch (FileStoreException ex) {
            assertTrue(ex.getMessage().startsWith("Invalid path"));
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception ex) {
            Assert.fail("Got generic exception");
        }
    }

    private String writeTemp(byte[] data) throws IOException {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(data)) {
            return fileStore.writeTemporaryFile(stream);
        }
    }

    private void write(String path, byte[] data) throws IOException {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(data)) {
            fileStore.write(path, stream);
        }
    }

    @SuppressWarnings("PMD.ReturnEmptyArrayRatherThanNull")
    private byte[] read(String path) throws IOException {
        try (InputStream in = fileStore.read(path)) {
            if (in != null) {
                return IOUtils.toByteArray(in);
            }
        }
        return null;
    }

    private String read(String path, Charset charset) throws IOException {
        try (InputStream in = fileStore.read(path)) {
            if (in != null) {
                return IOUtils.toString(in, charset);
            }
        }
        return null;
    }


}
