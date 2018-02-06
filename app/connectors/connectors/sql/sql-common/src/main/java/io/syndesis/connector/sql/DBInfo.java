package io.syndesis.connector.sql;

public class DBInfo {

    String catalog;
    String schema;
    String table;

    public DBInfo() {
        super();
    }

    public DBInfo(String catalog, String schema, String table) {
        super();
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
    }

}
