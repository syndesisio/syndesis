package io.syndesis.dv.lsp.parser.statement;

import static org.junit.Assert.assertEquals;

import org.eclipse.lsp4j.Position;
import org.junit.Test;

import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;

@SuppressWarnings("nls")
public class TestTokenContext {

	public CreateViewStatement createStatement;

    public void setCreateStatement(String stmt) {
        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);
        this.createStatement = new CreateViewStatement(analyzer);
    }

    public TokenContext getTokenContext(Position position) {
        return createStatement.getTokenContext(position);
    }

    public String posToString(Position pos) {
        return "Position[" + pos.getLine() + ", " + pos.getCharacter() + "]";
    }

    @Test
    public void testContextForAfterSelect() {
        String stmt =
                // CHARACTER--------->
                // 0-10 11-20 21-30 31-40 41-50 51-60 61-70
                // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW abcd ( id ) AS SELECT columnA as c1, columnB as c2 FROM bar";

        setCreateStatement(stmt);

        TokenContext tokenContext = getTokenContext(new Position(0, 42));
        assertEquals(DdlAnalyzerConstants.Context.SELECT_COLUMN, tokenContext.getContext());
    }

    @Test
    public void testContextForSelectFunction() {
        String stmt =
                // CHARACTER--------->
                // 10 20 30 40 50 60 70
                // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW abcd ( id ) AS SELECT concat(firstName, lastName) as c1 FROM names";

        setCreateStatement(stmt);

        TokenContext tokenContext = getTokenContext(new Position(0, 41));
        assertEquals(DdlAnalyzerConstants.Context.SELECT_COLUMN, tokenContext.getContext());
        tokenContext = getTokenContext(new Position(0, 60));
        assertEquals(DdlAnalyzerConstants.Context.SELECT_COLUMN, tokenContext.getContext());
    }

    @Test
    public void testContextForSelectFunctionNested() {
        String stmt =
        // CHARACTER--------->
//                 10        20        30        40        50        60        70        80        90        100       110       120       130
                // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
                // 123456789 123456789 123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW aaa (full_name) AS SELECT concat(t2.last_name, concat(', ', t2.first_name) ) as full_name FROM PostgresDB.contact as t2";

        setCreateStatement(stmt);

        TokenContext tokenContext = getTokenContext(new Position(0, 58));
        assertEquals(DdlAnalyzerConstants.Context.SELECT_COLUMN, tokenContext.getContext());
        tokenContext = getTokenContext(new Position(0, 59));
        assertEquals(DdlAnalyzerConstants.Context.SELECT_COLUMN, tokenContext.getContext());
    }

    @Test
    public void testContextForTableAliasDot() {
        String stmt =
        // CHARACTER--------->
//                 10        20        30        40        50        60        70        80        90        100       110       120       130
                // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
                // 123456789 123456789 123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW aaa (full_name) AS SELECT concat(t2. , param2) as xxx FROM PostgresDB.contact as t2";

        setCreateStatement(stmt);

        TokenContext tokenContext = getTokenContext(new Position(0, 47));
        assertEquals(DdlAnalyzerConstants.Context.SELECT_COLUMN, tokenContext.getContext());

        tokenContext = getTokenContext(new Position(0, 48));
        assertEquals(DdlAnalyzerConstants.Context.TABLE_ALIAS, tokenContext.getContext());
    }

    @Test
    public void testContextForTableAliasDot_1() {
        String stmt =
        // CHARACTER--------->
//                 10        20        30        40        50        60        70        80        90        100       110       120       130
                // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
                // 123456789 123456789 123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW aaa (full_name) AS SELECT t2.full_name, t2. FROM PostgresDB.contact as t2";

        setCreateStatement(stmt);

        TokenContext tokenContext = getTokenContext(new Position(0, 54));
        assertEquals(DdlAnalyzerConstants.Context.SELECT_COLUMN, tokenContext.getContext());

        tokenContext = getTokenContext(new Position(0, 55));
        assertEquals(DdlAnalyzerConstants.Context.TABLE_ALIAS, tokenContext.getContext());
    }

    @Test
    public void testContextForTableAliasDot_2() {
        String stmt =
        // CHARACTER--------->
//                 10        20        30        40        50        60        70        80        90        100       110       120       130
                // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
                // 123456789 123456789 123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW winelist (priceInCents, id, productcode) AS\n" + "  SELECT\n"
                        + "    t1.price * 100 as priceInCents, id, productcode, t2. \n" + "  FROM\n"
                        + "    PostgresDB.winelist AS t1, PostgresDB.contact as t2 WHERE id > 70 ORDER BY t1.id";

        setCreateStatement(stmt);

        TokenContext tokenContext = getTokenContext(new Position(2, 55));
        assertEquals(DdlAnalyzerConstants.Context.SELECT_COLUMN, tokenContext.getContext());

        tokenContext = getTokenContext(new Position(2, 56));
        assertEquals(DdlAnalyzerConstants.Context.TABLE_ALIAS, tokenContext.getContext());

    }

    @Test
    public void testContextForTableAliasDot_3() {
        // CHARACTER--------->
        // 10 20 30 40 50 60 70 80
        // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
        String stmt = "CREATE VIEW winelist AS SELECT * FROM PostgresDB.winelist as t1 WHERE id > 2000";
        setCreateStatement(stmt);

        TokenContext tokenContext = getTokenContext(new Position(0, 65));
        assertEquals(DdlAnalyzerConstants.Context.WHERE_CLAUSE, tokenContext.getContext());
    }

    @Test
    public void testContextForWhereAliasDot() {
        String stmt =
                // CHARACTER--------->
                // 10 20 30 40 50 60 70 80 90 100 110 120 130
                // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
                // 123456789 123456789 123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW winelist (priceInCents, id, productcode) AS\n" + "  SELECT\n"
                        + "    t1.price * 100 as priceInCents, id, productcode, t2.first_name\n" + "  FROM\n"
                        + "    PostgresDB.winelist AS t1, PostgresDB.contact as t2 WHERE t1. > 70 ORDER BY t1.id";

        setCreateStatement(stmt);

        TokenContext tokenContext = getTokenContext(new Position(4, 65));
        assertEquals(DdlAnalyzerConstants.Context.WHERE_CLAUSE_TABLE_ALIAS, tokenContext.getContext());
    }

    @Test
    public void testContextForSecondTableAfterComma() {
        // CHARACTER--------->
        // 10 20 30 40 50 60 70 80
        // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
        String stmt = "CREATE VIEW winelist AS SELECT * FROM PostgresDB.winelist as t1, ";
        setCreateStatement(stmt);

        TokenContext tokenContext = getTokenContext(new Position(0, 65));
        assertEquals(DdlAnalyzerConstants.Context.FROM_CLAUSE, tokenContext.getContext());
    }

    @Test
    public void testContextAfterSecondTableAndSpace() {
        // CHARACTER--------->
        // 10 20 30 40 50 60 70 80

        String stmt =

                "CREATE VIEW winelist AS SELECT * FROM PostgresDB.winelist as t1, PostgresDB.contact as t1 ";
        // 10 20 30 40 50 60 70 80
        // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
        // 123456789 123456789 123456789
        setCreateStatement(stmt);

        TokenContext tokenContext = getTokenContext(new Position(0, 89));
        assertEquals(DdlAnalyzerConstants.Context.QUERY_EXPRESSION, tokenContext.getContext());
    }

    /*
    @Test
    public void testContextsInFromClause() {

        String DDL_1 = "CREATE VIEW contact ( id ) AS SELECT t1.id FROM MyDB.id ";
        String DDL_2 = "CREATE VIEW contact ( id ) AS SELECT t1.id FROM MyDB.id, ";
        String DDL_3 = "CREATE VIEW contact ( id ) AS SELECT t1.id FROM MyDB.id AS ";
        String DDL_4 = "CREATE VIEW contact ( id ) AS SELECT t1.id FROM MyDB.id AS t1 ";
        String DDL_5 = "CREATE VIEW contact ( id ) AS SELECT t1.id FROM MyDB.id AS t1, ";
        String DDL_6 = "CREATE VIEW contact ( id ) AS SELECT t1.id FROM MyDB.id AS t1 WHERE ";
        String DDL_7 = "CREATE VIEW contact ( id ) AS SELECT t1.id FROM MyDB.id AS t1 WHERE t1.id";
        String DDL_8 = "CREATE VIEW someView AS SELECT * FROM schemaA.table_1 AS t1, schemaB.table_2 as t2 WHERE t1. ";
        			 // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
        // 123456789 123456789 123456789
        // 10 20 30 40 50 60 70 80 90

        testAndPrintContexts(DDL_1, 1);
        testAndPrintContexts(DDL_2, 51);
        testAndPrintContexts(DDL_3, 51);
        testAndPrintContexts(DDL_4, 51);
        testAndPrintContexts(DDL_5, 51);
        testAndPrintContexts(DDL_6, 51);
        testAndPrintContexts(DDL_7, 51);
        testAndPrintContexts(DDL_8, 68);
    }

    public void testAndPrintContexts(String stmt, int start) {
        setCreateStatement(stmt);
        int end = stmt.length();
        System.out.println("\nSTATEMENT: " + stmt);
        for (int i = start; i < end; i++) {
            Position pos = new Position(0, i);
            Token token = createStatement.getAnalyzer().getTokenAt(pos);
            String tokenStr = "[null]";
            if (token != null) {
                tokenStr = token.image;
            }
            char character = stmt.charAt(i);
            TokenContext tokenContext = getTokenContext(pos);
            System.out.println(posToString(pos) + "\t char: [ " + character + " ]  Token = " + tokenStr + " Context = "
                    + tokenContext.contextToString());
        }
        System.out.println("-------------------------------------------------------------------------");
    }
    */

}
