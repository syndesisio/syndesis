package io.syndesis.connector.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class SqlStatementMetaData {

    private Enum<StatementType> statementType;
    private List<SqlParam> inParams = new ArrayList<>();
    private List<SqlParam> outParams = new ArrayList<>();
    private List<String> tableNames = new ArrayList<>();
    private String sqlStatement;
    private String camelSqlStatement;
    private Set<String> tablesInSchema;
    private String schema;
    private String defaultedSqlStatement;

    public SqlStatementMetaData(String sqlStatement) {
        super();
        this.sqlStatement = sqlStatement;
    }

    public String addTable(String tableName) throws SQLException {
        if (tablesInSchema.contains(tableName)) {
            tableNames.add(tableName);
        } else {
            throw new SQLException("Table does not exist in schema " + schema);
        }
        return tableName;
    }

    public boolean hasInputParams() {
        return sqlStatement.contains(":#");
    }

    public int numberOfInputParams() {
        int fromIndex = 0;
        int numberOfInputParams=0;
        while (fromIndex >= 0) {
            fromIndex = sqlStatement.indexOf(":",fromIndex);
            numberOfInputParams++;
        }
        return numberOfInputParams;
    }
    
    public String getCamelSqlStatement() {
        if (camelSqlStatement == null) {
            camelSqlStatement = sqlStatement;
            for (SqlParam param : inParams) {
                camelSqlStatement = camelSqlStatement.replace(":" + param.getName(), ":#" + param.getName());
            }
        }
        return camelSqlStatement;
    }
    
    @SuppressWarnings("rawtypes")
    public String getDefaultedSqlStatement() {
        final List<Class> stringTypes = Arrays.asList(String.class, Character.class);
        if (defaultedSqlStatement == null) {
            defaultedSqlStatement = sqlStatement;
            for (SqlParam param : inParams) {
                if (stringTypes.contains(param.getTypeValue().getClazz())) {
                    defaultedSqlStatement = defaultedSqlStatement.replace(":#" + 
                            param.getName(), "'" + param.getTypeValue().getSampleValue().toString() + "'");
                } else {
                    defaultedSqlStatement = defaultedSqlStatement.replace(":#" + 
                            param.getName(), param.getTypeValue().getSampleValue().toString());
                }
            }
        }
        return defaultedSqlStatement;
    }

}