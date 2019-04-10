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
package io.syndesis.connector.sql.stored;

public class SampleStoredProcedures {

    /**
     * SQL to create the DEMO_ADD procedure in Apache Derby
     */
    public static String DERBY_DEMO_ADD_SQL =
            "CREATE PROCEDURE DEMO_ADD( IN A INTEGER, IN B INTEGER, OUT C INTEGER ) " +
            "PARAMETER STYLE JAVA " +
            "LANGUAGE JAVA " +
            "EXTERNAL NAME '" + SampleStoredProcedures.class.getName() + ".demo_add'";
    public static String DERBY_DEMO_OUT_SQL =
            "CREATE PROCEDURE DEMO_OUT( OUT C INTEGER ) " +
            "PARAMETER STYLE JAVA " +
            "LANGUAGE JAVA " +
            "EXTERNAL NAME '" + SampleStoredProcedures.class.getName() + ".demo_out'";

    /**
     * SQL to create the DEMO_ADD procedure in Oracle
     */
    public static String ORACLE_DEMO_ADD_SQL =
            "create or replace PROCEDURE DEMO_ADD \n" +
            "(\n" +
            "  A IN INTEGER\n" +
            ", B IN INTEGER \n" +
            ", C OUT INTEGER \n" +
            ") AS \n" +
            "BEGIN\n" +
            "  c := a + b;\n" +
            "END DEMO_ADD;";
    /**
     * SQL to create the DEMO_APP procedure in Postgresql
     */
    public static String POSTGRES_DEMO_ADD_SQL =
            "CREATE OR REPLACE FUNCTION public.demo_add(\n" +
            "    a numeric,\n" +
            "    b numeric,\n" +
            "    OUT c numeric)\n" +
            "    RETURNS numeric\n" +
            "    LANGUAGE 'plpgsql'\n" +
            "\n" +
            "AS $BODY$\n" +
            "\n" +
            "BEGIN\n" +
            " c := a + b;\n" +
            " return;\n" +
            "END; \n" +
            "$BODY$;\n" +
            "\n" +
            "ALTER FUNCTION public.demo_add(numeric, numeric)\n" +
            "    OWNER TO postgres;";
    /**
     * Java method implementing the Stored Procedure for Derby.
     *
     * @param a - input parameter of type integer
     * @param b - input parameter of type integer
     * @param c - output (the result of a + b) of type integer[]
     */
    public static void demo_add(
            int a /* IN parameter */,
            int b /* IN parameter */,
            int[] c /* OUT parameter */) {

        c[0] = a + b;
    }
    /**
     * Java method implementing the Stored Procedure for Derby.
     *
     * @param c - output (the result of a + b) of type integer[]
     */
    public static void demo_out(
            int[] c /* OUT parameter */) {

        c[0] = 60;
    }
}
