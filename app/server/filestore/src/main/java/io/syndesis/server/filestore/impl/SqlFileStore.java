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
package io.syndesis.server.filestore.impl;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;

import io.syndesis.server.dao.DaoException;

/**
 * Implementation of a {@code ExtensionDataAccessObject} backed by a SQL database.
 */
@SuppressWarnings("PMD.GodClass")
public class SqlFileStore {

    enum DatabaseKind {
        PostgreSQL, H2, Apache_Derby
    }

    private final DBI dbi;

    private final DatabaseKind databaseKind;

    public SqlFileStore(DBI dbi) {
        this.dbi = dbi;

        this.databaseKind = dbi.inTransaction((h, s) -> {
            String dbName = h.getConnection().getMetaData().getDatabaseProductName();
            return DatabaseKind.valueOf(dbName.replace(" ", "_"));
        });
    }

    public void init() {
        boolean needsInitialization = !dbi.inTransaction((h, s) -> tableExists(h, "filestore"));

        if (needsInitialization) {
            try {
                dbi.useHandle(h -> {
                    if (databaseKind == DatabaseKind.PostgreSQL) {
                        h.execute("CREATE TABLE filestore (path VARCHAR COLLATE \"C\" PRIMARY KEY, data OID)");
                    } else if (databaseKind == DatabaseKind.H2) {
                        h.execute("CREATE TABLE filestore (path VARCHAR PRIMARY KEY, data BLOB)");
                    } else if (databaseKind == DatabaseKind.Apache_Derby) {
                        h.execute("CREATE TABLE filestore (path VARCHAR(1000), data BLOB, PRIMARY KEY (path))");
                    } else {
                        throw new DaoException("Unsupported database kind: " + databaseKind);
                    }
                });
            } catch (CallbackFailedException ex) {
                throw new DaoException("Unable to initialize the filestore", ex);
            }
        }
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void destroy() {
        try {
            dbi.useHandle(h -> h.execute("DROP TABLE filestore"));
        } catch (CallbackFailedException ex) {
            // simply ignore
        }
    }

    public void write(String path, InputStream file) {
        FileStoreSupport.checkValidPath(path);
        Objects.requireNonNull(file, "file cannot be null");

        try {
            dbi.inTransaction((h, status) -> {
                doWrite(h, path, file);
                return true;
            });
        } catch (CallbackFailedException ex) {
            throw new DaoException("Unable to write on path " + path, ex);
        }
    }

    public String writeTemporaryFile(InputStream file) {
        Objects.requireNonNull(file, "file cannot be null");

        try {
            return dbi.inTransaction((h, status) -> {
                String path = newRandomTempFilePath();
                doWrite(h, path, file);
                return path;
            });
        } catch (CallbackFailedException ex) {
            throw new DaoException("Unable to write on temporary path", ex);
        }
    }

    public InputStream read(String path) {
        FileStoreSupport.checkValidPath(path);

        try {
            if (databaseKind == DatabaseKind.PostgreSQL) {
                return doReadPostgres(path);
            } else if (databaseKind == DatabaseKind.Apache_Derby) {
                return doReadDerby(path);
            } else {
                return dbi.inTransaction((h, status) -> doReadStandard(h, path));
            }
        } catch (CallbackFailedException ex) {
            throw new DaoException("Unable to read data from path " + path, ex);
        }
    }

    public boolean move(String fromPath, String toPath) {
        FileStoreSupport.checkValidPath(fromPath);
        FileStoreSupport.checkValidPath(toPath);

        try {
            return dbi.inTransaction((h, status) -> {
                boolean existed = h.select("SELECT 1 from filestore WHERE path=?", fromPath).size() > 0;
                if (existed) {
                    doDelete(h, toPath);
                    h.update("UPDATE filestore SET path=? WHERE path=?", toPath, fromPath);
                }

                return existed;
            });
        } catch (CallbackFailedException ex) {
            throw new DaoException("Unable to move file from path " + fromPath + " to path " + toPath, ex);
        }
    }

    public boolean delete(String path) {
        FileStoreSupport.checkValidPath(path);

        try {
            return dbi.inTransaction((h, status) -> doDelete(h, path));
        } catch (CallbackFailedException ex) {
            throw new DaoException("Unable to delete path " + path, ex);
        }
    }

    // ============================================================

    private void doWrite(Handle h, String path, InputStream file) {
        if (databaseKind == DatabaseKind.PostgreSQL) {
            doWritePostgres(h, path, file);
        } else if (databaseKind == DatabaseKind.Apache_Derby) {
            doWriteDerby(h, path, file);
        } else {
            doWriteStandard(h, path, file);
        }
    }

    private void doWriteStandard(Handle h, String path, InputStream file) {
        doDelete(h, path);
        h.insert("INSERT INTO filestore(path, data) values (?,?)", path, file);
    }

    private void doWritePostgres(Handle h, String path, InputStream file) {
        doDelete(h, path);
        try {
            LargeObjectManager lobj = getPostgresConnection(h.getConnection()).getLargeObjectAPI();
            long oid = lobj.createLO();
            LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
            try (OutputStream lob = obj.getOutputStream()) {
                IOUtils.copy(file, lob);
            }

            h.insert("INSERT INTO filestore(path, data) values (?,?)", path, oid);
        } catch (IOException | SQLException ex) {
            throw DaoException.launderThrowable(ex);
        }
    }

    private void doWriteDerby(Handle h, String path, InputStream file) {
        doDelete(h, path);
        try {
            Blob blob = h.getConnection().createBlob();
            try (OutputStream out = blob.setBinaryStream(1)) {
                IOUtils.copy(file, out);
            }

            h.insert("INSERT INTO filestore(path, data) values (?,?)", path, blob);
        } catch (IOException | SQLException ex) {
            throw DaoException.launderThrowable(ex);
        }
    }

    private InputStream doReadStandard(Handle h, String path) {
        List<Map<String, Object>> res = h.select("SELECT data FROM filestore WHERE path=?", path);

        Optional<Blob> blob = res.stream()
            .map(row -> row.get("data"))
            .map(Blob.class::cast)
            .findFirst();

        if (blob.isPresent()) {
            try {
                return blob.get().getBinaryStream();
            } catch (SQLException ex) {
                throw new DaoException("Unable to read from BLOB", ex);
            }
        }

        return null;
    }

    /**
     * Derby does not allow to read from the blob after the connection has been closed.
     * It also requires an outcome of commit/rollback.
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    private InputStream doReadDerby(String path) {
        Handle h = dbi.open();
        try {
            h.getConnection().setAutoCommit(false);

            List<Map<String, Object>> res = h.select("SELECT data FROM filestore WHERE path=?", path);

            Optional<Blob> blob = res.stream()
                .map(row -> row.get("data"))
                .map(Blob.class::cast)
                .findFirst();

            if (blob.isPresent()) {
                return new HandleCloserInputStream(h, blob.get().getBinaryStream());
            } else {
                h.commit();
                h.close();
                return null;
            }

        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            // Do cleanup
            try {
                h.rollback();
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception ex) {
                // ignore
            }
            IOUtils.closeQuietly(h);

            throw DaoException.launderThrowable(e);
        }
    }

    /**
     * Postgres does not allow to read from the large object after the connection has been closed.
     */
    private InputStream doReadPostgres(String path) {
        Handle h = dbi.open();
        try {
            h.getConnection().setAutoCommit(false);

            List<Map<String, Object>> res = h.select("SELECT data FROM filestore WHERE path=?", path);

            Optional<Long> oid = res.stream()
                .map(row -> row.get("data"))
                .map(Long.class::cast)
                .findFirst();

            if (oid.isPresent()) {
                LargeObjectManager lobj = getPostgresConnection(h.getConnection()).getLargeObjectAPI();
                LargeObject obj = lobj.open(oid.get(), LargeObjectManager.READ);
                return new HandleCloserInputStream(h, obj.getInputStream());
            } else {
                h.close();
                return null;
            }

        } catch (SQLException e) {
            IOUtils.closeQuietly(h);
            throw DaoException.launderThrowable(e);
        }
    }

    private boolean doDelete(Handle h, String path) {
        return h.update("DELETE FROM filestore WHERE path=?", path) > 0;
    }

    private PGConnection getPostgresConnection(Connection conn) throws SQLException {
        if (conn instanceof PGConnection) {
            return PGConnection.class.cast(conn);
        }
        return conn.unwrap(PGConnection.class);
    }

    private String newRandomTempFilePath() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.ROOT);
        return "/tmp/" + fmt.format(new Date()) + "_" + UUID.randomUUID();
    }

    private boolean tableExists(Handle h, String tableName) {
        try {
            String tableToCheck = tableName;
            boolean caseSensitive = this.databaseKind == DatabaseKind.PostgreSQL;
            if (!caseSensitive) {
                tableToCheck = tableName.toUpperCase(Locale.ROOT);
            }
            DatabaseMetaData metaData = h.getConnection().getMetaData();

            try (ResultSet rs = metaData.getTables(null, null, tableToCheck, null)) {
                while (rs.next()) {
                    String foundTable = rs.getString("TABLE_NAME");
                    if (tableToCheck.equalsIgnoreCase(foundTable)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException ex) {
            throw DaoException.launderThrowable("Cannot check if the table " + tableName + " already exists", ex);
        }
    }

    /**
     * Allows closing a handle after the given {@link InputStream} has been fully read and closed.
     */
    static class HandleCloserInputStream extends FilterInputStream {

        private Handle handle;

        public HandleCloserInputStream(Handle handle, InputStream in) {
            super(in);
            this.handle = handle;
        }

        @Override
        @SuppressWarnings("PMD.EmptyCatchBlock")
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                try {
                    handle.commit();
                } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception ex) {
                    // ignore
                }
                handle.close();
            }
        }
    }

}
