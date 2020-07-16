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
                  "CREATE VIEW winelist (priceInDollars, id) AS\n" 
                + "  SELECT\n"
                + "    t1.price AS priceInDollars, t1.id, t2. \n"
                + "  FROM\n"
                + "    PostgresDB.winelist AS t1, PostgresDB.contact as t2";

        setCreateStatement(stmt);

        TokenContext tokenContext = getTokenContext(new Position(2, 41));
        assertEquals(DdlAnalyzerConstants.Context.SELECT_COLUMN, tokenContext.getContext());

        tokenContext = getTokenContext(new Position(2, 42));
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
                //           10        20        30        40        50        60        70
                // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789
                // 123456789 123456789 123456789 123456789 123456789 123456789 123456789
                  "CREATE VIEW winelist (priceInCents, id, productcode) AS\n"
                + "  SELECT\n"
                + "    t1.price as priceInCents, id, productcode, t2.first_name\n"
                + "  FROM\n"
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
              //            10        20        30        40        50        60        70        80
              // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789
        setCreateStatement(stmt);

        TokenContext tokenContext = getTokenContext(new Position(0, 89));
        assertEquals(DdlAnalyzerConstants.Context.WHERE_CLAUSE_START, tokenContext.getContext());
    }

}
