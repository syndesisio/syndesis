package io.syndesis.verifier.v1.metadata;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.syndesis.connector.DatabaseProduct;
import io.syndesis.connector.SampleStoredProcedures;
import io.syndesis.connector.SqlStoredConnectorMetaDataExtension;

public class SqlStoredMetadataAdapterTest {

    private static Connection connection;
    private static Properties properties = new Properties();
    private static String DERBY_DEMO_ADD2_SQL = 
            "CREATE PROCEDURE DEMO_ADD2( IN A INTEGER, IN B INTEGER, OUT C INTEGER ) " +
            "PARAMETER STYLE JAVA " +
            "LANGUAGE JAVA " +
            "EXTERNAL NAME 'io.syndesis.connector.SampleStoredProcedures.demo_add'";
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        InputStream is = SqlStoredMetadataAdapterTest.class.getClassLoader().getResourceAsStream("application.properties");
        properties.load(is);
        String user     = String.valueOf(properties.get("sql-stored-connector.user"));
        String password = String.valueOf(properties.get("sql-stored-connector.password"));
        String url      = String.valueOf(properties.get("sql-stored-connector.url"));
        
        System.out.println("Connecting to the database for unit tests");
        try {
            connection = DriverManager.getConnection(url,user,password);
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            Map<String,Object> parameters = new HashMap<String,Object>();
            for (final String name: properties.stringPropertyNames()) {
                parameters.put(name.substring(name.indexOf(".")+1), properties.getProperty(name));
            }
            if (databaseProductName.equalsIgnoreCase(DatabaseProduct.APACHE_DERBY.nameWithSpaces())) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute(SampleStoredProcedures.DERBY_DEMO_ADD_SQL);
                    System.out.println("Created procedure " + SampleStoredProcedures.DERBY_DEMO_ADD_SQL);
                    stmt.execute(DERBY_DEMO_ADD2_SQL);
                    System.out.println("Created procedure " + DERBY_DEMO_ADD2_SQL);
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Exception during Stored Procedure Creation.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Exception");
        }
    }
    
    @AfterClass
    public static void afterClass() throws SQLException {
        if (connection!=null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void adaptTest() throws IOException {
        
        SqlStoredConnectorMetaDataExtension ext = new SqlStoredConnectorMetaDataExtension();
        Map<String,Object> parameters = new HashMap<String,Object>();
        for (final String name: properties.stringPropertyNames()) {
            parameters.put(name.substring(name.indexOf(".")+1), properties.getProperty(name));
        }
        Optional<MetaData> metadata = ext.meta(parameters);
        
        SqlStoredMetadataAdapter adapter = new SqlStoredMetadataAdapter();
        SyndesisMetadata<String> syndesisMetaData = adapter.adapt(parameters, metadata.get());
        
        ObjectMapper mapper = new ObjectMapper();
        String expectedListOfProcedures = IOUtils.toString((this.getClass().getResource("/sql/stored_procedure_list.json")));
        String actualListOfProcedures = (mapper.writerWithDefaultPrettyPrinter().writeValueAsString(syndesisMetaData.properties));
        System.out.println("----------- list of procedures ------------");
        System.out.println(actualListOfProcedures);
        assertEquals(expectedListOfProcedures, actualListOfProcedures);
        
        parameters.put("procedureName", "DEMO_ADD");
        SyndesisMetadata<String> syndesisMetaData2 = adapter.adapt(parameters, metadata.get());
        String expectedProperties = IOUtils.toString((this.getClass().getResource("/sql/demo_add_properties.json")));
        String actualProperties = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(syndesisMetaData2.properties);
        assertEquals(expectedProperties, actualProperties);
        System.out.println("----------- DEMO_ADD properties ------------");
        System.out.println(actualProperties);
        
        String expectedDatashapeIn = IOUtils.toString((this.getClass().getResource("/sql/demo_add_datashape_in.json")));
        String actualDatashapeIn = syndesisMetaData2.inputSchema;
        assertEquals(expectedDatashapeIn, actualDatashapeIn);
        System.out.println("----------- inputSchema ------------");
        System.out.println(syndesisMetaData2.inputSchema);
        
        String expectedDatashapeOut = IOUtils.toString((this.getClass().getResource("/sql/demo_add_datashape_out.json")));
        String actualDatashapeOut = syndesisMetaData2.outputSchema;
        assertEquals(expectedDatashapeOut, actualDatashapeOut);
        System.out.println("----------- outputSchema ------------");
        System.out.println(actualDatashapeOut);
    }

}
