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
package io.syndesis.filestore.impl;

import io.syndesis.dao.icon.IconDataAccessException;
import io.syndesis.dao.icon.IconDataAccessObject;
import org.apache.commons.io.IOUtils;
import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.CallbackFailedException;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of a {@code IconDataAccessObject} backed by a SQL database.
 */
@SuppressWarnings("PMD.GodClass")
public class SqlIconFileStore implements IconDataAccessObject {

    enum DatabaseKind {
        PostgreSQL, H2, Apache_Derby
    }

    private final DBI dbi;

    private DatabaseKind databaseKind;

    public SqlIconFileStore(DBI dbi) {
        this.dbi = dbi;

        this.databaseKind = dbi.inTransaction((h, s) -> {
            String dbName = h.getConnection().getMetaData().getDatabaseProductName();
            return DatabaseKind.valueOf(dbName.replace(" ", "_"));
        });
    }

    @Override
    public void init() {
        boolean needsInitialization = !dbi.inTransaction((h, s) -> tableExists(h, "filestore"));

        if (needsInitialization) {
            try {
                dbi.useHandle(h -> {
                    if (databaseKind == DatabaseKind.PostgreSQL) {
                        h.execute("CREATE TABLE icon_filestore (id VARCHAR COLLATE \"C\" PRIMARY KEY, data OID)");
                    } else if (databaseKind == DatabaseKind.H2) {
                        h.execute("CREATE TABLE icon_filestore (id VARCHAR PRIMARY KEY, data BLOB)");
                    } else if (databaseKind == DatabaseKind.Apache_Derby) {
                        h.execute("CREATE TABLE icon_filestore (id VARCHAR(1000), data BLOB, PRIMARY KEY (path))");
                    } else {
                        throw new IconDataAccessException("Unsupported database kind: " + databaseKind);
                    }
                });
            } catch (CallbackFailedException ex) {
                throw new IconDataAccessException("Unable to initialize the icon_filestore", ex);
            }
        }
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    public void destroy() {
        try {
            dbi.useHandle(h -> h.execute("DROP TABLE icon_filestore"));
        } catch (CallbackFailedException ex) {
            // simply ignore
        }
    }

    @Override
    public void write(String id, InputStream file) {
        Objects.requireNonNull(file, "file cannot be null");

        try {
            dbi.inTransaction((h, status) -> {
                doWrite(h, id, file);
                return true;
            });
        } catch (CallbackFailedException ex) {
            throw new IconDataAccessException("Unable to write on id " + id, ex);
        }
    }

    @Override
    public InputStream read(String id) {
        try {
            if (databaseKind == DatabaseKind.PostgreSQL) {
                return doReadPostgres(id);
            } else if (databaseKind == DatabaseKind.Apache_Derby) {
                return doReadDerby(id);
            } else {
                return dbi.inTransaction((h, status) -> doReadStandard(h, id));
            }
        } catch (CallbackFailedException ex) {
            throw new IconDataAccessException("Unable to read data from id " + id, ex);
        }
    }

    @Override
    public boolean delete(String id) {
        try {
            return dbi.inTransaction((h, status) -> doDelete(h, id));
        } catch (CallbackFailedException ex) {
            throw new IconDataAccessException("Unable to delete id " + id, ex);
        }
    }

    // ============================================================

    private void doWrite(Handle h, String id, InputStream file) {
        if (databaseKind == DatabaseKind.PostgreSQL) {
            doWritePostgres(h, id, file);
        } else if (databaseKind == DatabaseKind.Apache_Derby) {
            doWriteDerby(h, id, file);
        } else {
            doWriteStandard(h, id, file);
        }
    }

    private void doWriteStandard(Handle h, String id, InputStream file) {
        doDelete(h, id);
        h.insert("INSERT INTO icon_filestore(id, data) values (?,?)", id, file);
    }

    private void doWritePostgres(Handle h, String id, InputStream file) {
        doDelete(h, id);
        try {
            LargeObjectManager lobj = getPostgresConnection(h.getConnection()).getLargeObjectAPI();
            long oid = lobj.createLO();
            LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
            try (OutputStream lob = obj.getOutputStream()) {
                IOUtils.copy(file, lob);
            }

            h.insert("INSERT INTO icon_filestore(id, data) values (?,?)", id, oid);
        } catch (IOException | SQLException ex) {
            throw IconDataAccessException.launderThrowable(ex);
        }
    }

    private void doWriteDerby(Handle h, String id, InputStream file) {
        doDelete(h, id);
        try {
            Blob blob = h.getConnection().createBlob();
            try (OutputStream out = blob.setBinaryStream(1)) {
                IOUtils.copy(file, out);
            }

            h.insert("INSERT INTO icon_filestore(id, data) values (?,?)", id, blob);
        } catch (IOException | SQLException ex) {
            throw IconDataAccessException.launderThrowable(ex);
        }
    }

    private InputStream doReadStandard(Handle h, String id) {
        List<Map<String, Object>> res = h.select("SELECT data FROM icon_filestore WHERE id=?", id);

        Optional<Blob> blob = res.stream()
            .map(row -> row.get("data"))
            .map(Blob.class::cast)
            .findFirst();

        if (blob.isPresent()) {
            try {
                return blob.get().getBinaryStream();
            } catch (SQLException ex) {
                throw new IconDataAccessException("Unable to read from BLOB", ex);
            }
        }

        return null;
    }

    /**
     * Derby does not allow to read from the blob after the connection has been closed.
     * It also requires an outcome of commit/rollback.
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    private InputStream doReadDerby(String id) {
        Handle h = dbi.open();
        try {
            h.getConnection().setAutoCommit(false);

            List<Map<String, Object>> res = h.select("SELECT data FROM icon_filestore WHERE id=?", id);

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

            throw IconDataAccessException.launderThrowable(e);
        }
    }

    /**
     * Postgres does not allow to read from the large object after the connection has been closed.
     */
    private InputStream doReadPostgres(String id) {
        Handle h = dbi.open();
        try {
            h.getConnection().setAutoCommit(false);

            List<Map<String, Object>> res = h.select("SELECT data FROM icon_filestore WHERE id=?", id);

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
            throw IconDataAccessException.launderThrowable(e);
        }
    }

    private boolean doDelete(Handle h, String id) {
        return h.update("DELETE FROM icon_filestore WHERE id=?", id) > 0;
    }

    private PGConnection getPostgresConnection(Connection conn) throws SQLException {
        if (conn instanceof PGConnection) {
            return PGConnection.class.cast(conn);
        }
        return conn.unwrap(PGConnection.class);
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
            throw IconDataAccessException.launderThrowable("Cannot check if the table " + tableName + " already exists", ex);
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
