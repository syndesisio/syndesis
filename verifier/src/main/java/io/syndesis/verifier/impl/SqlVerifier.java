package io.syndesis.verifier.impl;

import io.syndesis.connector.sql.SqlConnectorVerifierExtension;

import org.springframework.stereotype.Component;

/**
 * @author kstam
 * @since 8/29/2017
 */
@Component("sql")
public class SqlVerifier extends BaseVerifier {

    public SqlVerifier() {
        super(SqlConnectorVerifierExtension.class);
    }

    @Override
    protected String getConnectorAction() {
        return "sql";
    }

}
