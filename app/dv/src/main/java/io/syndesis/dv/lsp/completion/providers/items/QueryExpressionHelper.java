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
package io.syndesis.dv.lsp.completion.providers.items;

public class QueryExpressionHelper {

    /*
     * CREATE VIEW TEMPLATE == SAMPLE STATEMENT ==
     *
     * CREATE VIEW customerInfo ( first_name string(255) } AS SELECT * FROM customer
     *
     * == DESIRED TEMPLATE FORMAT ==
     *
     * CREATE VIEW $view_name ( $column_name $datatype ) AS SELECT * FROM
     * $table_name
     *
     */
    public static final String CREATE_VIEW_INSERT_TEXT = "CREATE VIEW ${1:view_name} (\n\t${2:column_name} ${3:datatype}\n"
            + ") AS SELECT * FROM ${4:table_name};";

    /*
     * INNER JOIN EXAMPLE
     *
     * >> SAMPLE STATEMENT
     *
     * SELECT product_name, category_name, list_price FROM production.products p
     * INNER JOIN production.categories c ON c.category_id = p.category_id
     *
     * >> DESIRED TEMPLATE FORMAT
     *
     * CREATE VIEW view_name ( ) AS SELECT * FROM table1_name AS t1 INNER JOIN
     * table2_name AS t2 ON t1.column_a = t2.column_b
     *
     */
    public static final String CREATE_VIEW_INNER_JOIN_INSERT_TEXT = "CREATE VIEW ${1:view_name} (\n) AS SELECT * FROM ${2:table1_name} AS t1\n"
            + "INNER JOIN ${3:table2_name} AS t2\n" + "\tON t1.${4:column_a} = t2.${5:column_b};";

    /*
     * SIMPLE JOIN or LEFT JOIN
     *
     * >> SAMPLE STATEMENT
     *
     * CREATE VIEW studentInfo ( CourseID integer, StudentName string ) AS SELECT
     * StudentCourse.CourseID,Student.StudentName FROM Student INNER JOIN
     * StudentCourse ON StudentCourse.EnrollNo = Student.EnrollNo ORDER BY
     * StudentCourse.CourseID
     *
     * >> DESIRED TEMPLATE FORMAT
     *
     * CREATE VIEW view_name ( '/'* add column definitions *'/' ) AS SELECT * FROM
     * table1_name AS t1 JOIN ${3:table2_name} AS t2 ON t2.column_a = t1.column_b
     * ORDER BY t2.column_b
     *
     */
    public static final String CREATE_VIEW_LEFT_OUTER_JOIN_INSERT_TEXT = "CREATE VIEW ${1:view_name} (\n\t/* add column definitions */\n) "
            + "AS SELECT * FROM ${2:table1_name} AS t1\nJOIN ${3:table2_name} "
            + "AS t2\n\tON t2.${4:column_a} = t1.${5:column_b}\nORDER BY t2.${5:column_b};";

}
