package io.syndesis.verifier.impl;


import io.syndesis.connector.sql.stored.SqlStoredConnectorVerifierExtension;
import org.springframework.stereotype.Component;

/**
 * @author kstam
 * @since 8/29/2017
 */
@Component("sql-stored-connector")
public class SqlStoredVerifier extends BaseVerifier {

    public SqlStoredVerifier() {
        super(SqlStoredConnectorVerifierExtension.class);
    }

    @Override
    protected String getConnectorAction() {
        return "sql-stored-connector";
    }

}
