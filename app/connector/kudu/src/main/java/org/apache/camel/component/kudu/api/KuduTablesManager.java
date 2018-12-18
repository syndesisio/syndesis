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

package org.apache.camel.component.kudu.api;

import org.apache.kudu.client.*;
import java.util.Map;

/**
 * Kudu table manager
 *
 * Provides operations to manage kudu tables and rows
 */
public class KuduTablesManager {
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
    public KuduTable createTable(KuduModelTable table) {
        if (table == null) {
            throw new IllegalArgumentException("Parameter 'table' can not be null");
        }

        // Set up a simple schema.
        try {
            // Create the table.
            KuduTable t = client.createTable( table.getName(), table.getSchema(), table.getCto());
            return t;
        } catch (KuduException e) {
            throw new RuntimeException(
                    String.format("Kudu API returned the error \n message: %s\n\n cause: %s", e.getMessage(), e.getCause()));
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

        try {
            DeleteTableResponse r = client.deleteTable(tableName);
            return r;
        } catch (KuduException e) {
            throw new RuntimeException(
                    String.format("Kudu API returned the error \n message: %s\n\n cause: %s", e.getMessage(), e.getCause()));
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
                org.apache.kudu.client.RowErrorsAndOverflowStatus roStatus = session.getPendingErrors();
                org.apache.kudu.client.RowError[] errs = roStatus.getRowErrors();
                int numErrs = Math.min(errs.length, 5);
                for (int i = 0; i < numErrs; i++) {
                }

                if (roStatus.isOverflowed()) {
                }
                throw new RuntimeException(
                        String.format("Error inserting row to Kudu table %s", tableName));
            }
            return session;
        } catch (KuduException e) {
            throw new RuntimeException(
                    String.format("Kudu API returned the error \n message: %s\n\n cause: %s", e.getMessage(), e.getCause()));
        }
    }
}
