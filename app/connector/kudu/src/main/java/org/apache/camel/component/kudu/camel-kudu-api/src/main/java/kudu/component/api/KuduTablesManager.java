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
package kudu.component.api;

import org.apache.kudu.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Kudu table manager
 * <p>
 * Provides operations to manage kudu tables and rows
 */
public class KuduTablesManager {
    private static final Logger LOG = LoggerFactory.getLogger(KuduTablesManager.class);
    private KuduClient client;

    public KuduTablesManager(KuduClient client) {
        this.client = client;
    }

    /**
     * Create a sample kudu table. This method is for testing purposes
     *
     * @param table The table to create
     * @return Table object
     */
    public KuduTable createTable(KuduApiTable table) {
        if (table == null) {
            throw new IllegalArgumentException("Parameter 'table' can not be null");
        }
        LOG.debug("Deleteing table {}", table.getName());

        // Set up a simple schema.
        try {
            // Create the table.
            KuduTable t = client.createTable(table.getName(), table.getSchema(), table.getCto());
            LOG.info("Kudu table {} successfully reated!", t.getName());
            return t;
        } catch (KuduException e) {
            LOG.error("Error creating kudu table {}", table.getName());
            throw new RuntimeException(
                    String.format("Kudu API returned the error %d\n\n%s", e.getMessage(), e.getCause()), e);
        }
    }

    /**
     * Delete a table from kudu based on tableName
     *
     * @param tableName The name of the table to delete
     * @return Kudu API response to table deletion
     */
    public DeleteTableResponse deleteTable(String tableName) {
        if (tableName == null) {
            throw new IllegalArgumentException("Parameter 'tableName' can not be null");
        }
        LOG.debug("Deleteing table {}", tableName);

        try {
            DeleteTableResponse r = client.deleteTable(tableName);
            LOG.info("Kudu table {} successfully deleted!", tableName);
            return r;
        } catch (KuduException e) {
            LOG.error("Error deleting kudu table" + tableName);
            throw new RuntimeException(
                    String.format("Kudu API returned the error %d\n\n%s", e.getMessage(), e.getCause()), e);
        }
    }

    /**
     * Insert a row in a table. The table must exist
     *
     * @param tableName - Table to use for insert operation
     * @param insertRow - Map<String, Object>, each key:value set correspond to a column of
     *                  the Insert operation where _key_ is the name of the column and value the corresponding
     *                  value
     * @return: The kudu session
     */
    public KuduSession insertRow(String tableName, Map<String, Object> insertRow) {
        try {
            LOG.debug("Inserting row {} into table {}", insertRow, tableName);
            if (tableName == null) {
                throw new IllegalArgumentException("Parameter 'tableName' can not be null");
            }
            if (insertRow == null || insertRow.isEmpty()) {
                throw new IllegalArgumentException("Parameter 'insertRow' can not be null or be empty");
            }

            // Open the table and create a KuduSession.
            KuduTable table = client.openTable(tableName);
            KuduSession session = client.newSession();

            Insert insert = table.newInsert();
            PartialRow row = insert.getRow();

            for (Map.Entry<String, Object> entry : insertRow.entrySet()) {
                LOG.debug(entry.getKey() + "/" + entry.getValue());
                switch (entry.getValue().getClass().getName()) {
                    case "java.lang.String":
                        row.addString(entry.getKey(), (String) entry.getValue());
                        break;
                    case "java.lang.Integer":
                        row.addInt(entry.getKey(), (Integer) entry.getValue());
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid row type " + entry.getValue().getClass().getName());
                }
            }
            session.apply(insert);

            /**
             * Call session.close() to end the session and ensure the rows are
             * flushed and errors are returned.
             * You can also call session.flush() to do the same without ending the session.
             * When flushing in AUTO_FLUSH_BACKGROUND mode (the default mode recommended
             * for most workloads, you must check the pending errors as shown below, since
             * write operations are flushed to Kudu in background threads.
             */
            session.close();

            if (session.countPendingErrors() != 0) {
                LOG.error("ERRORS inserting rows");
                org.apache.kudu.client.RowErrorsAndOverflowStatus roStatus = session.getPendingErrors();
                org.apache.kudu.client.RowError[] errs = roStatus.getRowErrors();
                int numErrs = Math.min(errs.length, 5);
                LOG.error("There were errors inserting rows to Kudu, the few first errors follow:");
                for (int i = 0; i < numErrs; i++) {
                    LOG.error(errs[i].getMessage());
                }

                if (roStatus.isOverflowed()) {
                    LOG.error("Error buffer overflowed: some errors were discarded");
                }
                throw new RuntimeException(
                        String.format("Error inserting row {} to Kudu table {}", insertRow, tableName));
            }
            LOG.info("Row successfully inserted");
            return session;
        } catch (KuduException e) {
            LOG.error("Error inserting row {} into table {}", insertRow, tableName);
            throw new RuntimeException(
                    String.format("Kudu API returned the error %d\n\n%s", e.getMessage(), e.getCause()), e);
        }
    }
}
