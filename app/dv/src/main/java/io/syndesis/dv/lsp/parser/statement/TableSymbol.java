package io.syndesis.dv.lsp.parser.statement;

import org.eclipse.lsp4j.Position;

import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class TableSymbol extends AbstractStatementObject {

    public TableSymbol(DdlTokenAnalyzer analyzer) {
        super(analyzer);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void parseAndValidate() {
        // TODO Auto-generated method stub
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        // TODO Auto-generated method stub
        return null;
    }

}
