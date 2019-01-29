package io.syndesis.connector.kudu;

import io.syndesis.common.util.Json;
import io.syndesis.connector.kudu.common.KuduSupport;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.kudu.ColumnSchema;
import org.apache.kudu.client.KuduClient;
import org.apache.kudu.client.KuduScanner;
import org.apache.kudu.client.KuduTable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class KuduScanCustomizerTest extends AbstractKuduCustomizerTestSupport {
    private KuduScanCustomizer customizer;
    private KuduClient connection;
    private static final String TABLE = "impala::default.syndesis_test";
    private static final String HOST = "quickstart.cloudera";
    private static final String PORT = "7051";

    @Before
    public void setupCustomizer() {
        this.customizer = new KuduScanCustomizer();
        connection = KuduSupport.createConnection(HOST, PORT);
    }

    @After
    public void shutDown() {
        try {
            connection.shutdown();
        } catch (Exception e) {

        }
    }

    @Test
    public void testBeforeConsumer() throws Exception {
        Map<String, Object> options = new HashMap<>();

        customizer.customize(getComponent(), options);

        KuduTable table = connection.openTable(TABLE);

        List<String> projectColumns = new ArrayList<>(1);
        Iterator<ColumnSchema> columns = table.getSchema().getColumns().iterator();

        while (columns.hasNext()) {
            projectColumns.add(columns.next().getName());
        }

        KuduScanner scanner = connection.newScannerBuilder(table)
                .setProjectedColumnNames(projectColumns)
                .build();

        Exchange inbound = new DefaultExchange(createCamelContext());
        inbound.getIn().setBody(scanner);
        getComponent().getBeforeConsumer().process(inbound);

        String body = (String) inbound.getIn().getBody();
        assertNotNull(body);

        Map<String, Object> map = Json.reader().forType(Map.class).readValue(body);
        assertEquals(46, map.get("id"));
        assertEquals(10, map.get("_integer"));
        assertEquals(556, map.get("_long"));
    }

}