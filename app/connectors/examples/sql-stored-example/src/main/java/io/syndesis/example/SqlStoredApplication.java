/**
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.syndesis.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.syndesis.connector.sql.DatabaseProduct;


//CHECKSTYLE:OFF
@SpringBootApplication
public class SqlStoredApplication {

    /**
     * A main method to start this application.
     * @throws IOException
     * @throws FileNotFoundException
     * @throws SQLException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException {

        Properties properties = new Properties();
        InputStream is = SqlStoredApplication.class.getClassLoader().getResourceAsStream("application.properties");
        properties.load(is);
        String url      = properties.getProperty("sql-stored-connector.url");
        String user     = properties.getProperty("sql-stored-connector.user");
        String password = properties.getProperty("sql-stored-connector.password");

        //create the stored procedure in the Derby
        try (Connection connection = DriverManager.getConnection(url,user,password)) {
            connection.setAutoCommit(true);
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            if (databaseProductName.equalsIgnoreCase(DatabaseProduct.APACHE_DERBY.nameWithSpaces())) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(SampleStoredProcedures.DERBY_DEMO_ADD_SQL);
                    System.out.println("Created procedure " + SampleStoredProcedures.DERBY_DEMO_ADD_SQL);
                    stmt.execute(SampleStoredProcedures.DERBY_DEMO_OUT_SQL);
                    System.out.println("Created procedure " + SampleStoredProcedures.DERBY_DEMO_OUT_SQL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        SpringApplication.run(SqlStoredApplication.class, args);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
//CHECKSTYLE:ON