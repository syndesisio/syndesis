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
package io.syndesis.common.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ErrorCategory {

    //Sydesis Categories
    public static final String SERVER_ERROR           = "SERVER_ERROR";
    public static final String CONNECTOR_ERROR        = "CONNECTOR_ERROR";
    public static final String ENTITY_NOT_FOUND_ERROR = "ENTITY_NOT_FOUND_ERROR";

    //Spring RuntimeExceptions Categories
    public static final String DATA_ACCESS_ERROR               = "DATA_ACCESS_ERROR";
    public static final String NON_TRANSIENT_DATA_ACCESS_ERROR = "NON_TRANSIENT_DATA_ACCESS_ERROR";
    public static final String DATA_INTEGRITY_VIOLATION_ERROR  = "DATA_INTEGRITY_VIOLATION_ERROR";
    public static final String DUPLICATE_KEY_ERROR             = "DUPLICATE_KEY_ERROR";
    public static final String TRANSIENT_DATA_ACCESS_ERROR     = "TRANSIENT_DATA_ACCESS_ERROR";

    public static final Map<String,String> RUNTIME_EXCEPTION_CATEGORY_MAP = initExceptionCategoryMap();
    public static final Map<String,String> CATEGORY_HIERACHY_MAP = initCategoryHierachyMap();

    private ErrorCategory() {}

    //Mapping Categories to their Parent
    private static Map<String,String> initCategoryHierachyMap() {
        Map<String,String> map = new HashMap<>();
        map.put(NON_TRANSIENT_DATA_ACCESS_ERROR, DATA_ACCESS_ERROR);
        map.put(DATA_INTEGRITY_VIOLATION_ERROR, NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put(DUPLICATE_KEY_ERROR, DATA_INTEGRITY_VIOLATION_ERROR);
        map.put(TRANSIENT_DATA_ACCESS_ERROR, DATA_ACCESS_ERROR);
        return Collections.unmodifiableMap(map);
    }

    //Mapping RuntimeExceptions to Categories
    private static Map<String,String> initExceptionCategoryMap() {
        Map<String,String> map = new HashMap<>();
        //DataAccessExceptions
        map.put("org.springframework.dao.DataAccessException", DATA_ACCESS_ERROR);
        //  NonTransientDataAccessExceptions
        map.put("org.springframework.dao.NonTransientDataAccessException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.CleanupFailureDataAccessException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.DataIntegrityViolationException", DATA_INTEGRITY_VIOLATION_ERROR);
        map.put("org.springframework.dao.DuplicateKeyException", DUPLICATE_KEY_ERROR);
        map.put("org.springframework.dao.DataRetrievalFailureException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.IncorrectResultSetColumnCountException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.IncorrectResultSizeDataAccessException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.LobRetrievalFailureException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.orm.ObjectRetrievalFailureException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.orm.hibernate5.HibernateObjectRetrievalFailureException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.orm.jpa.JpaObjectRetrievalFailureException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.InvalidDataAccessApiUsageException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.support.xml.SqlXmlFeatureNotImplementedException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.BadSqlGrammarException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jca.cci.CciOperationNotSupportedException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.orm.hibernate5.HibernateQueryException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.IncorrectUpdateSemanticsDataAccessException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.InvalidResultSetAccessException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jca.cci.InvalidResultSetAccessException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jca.cci.RecordTypeNotSupportedException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.TypeMismatchDataAccessException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.NonTransientDataAccessResourceException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.DataAccessResourceFailureException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jca.cci.CannotCreateRecordException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jca.cci.CannotGetCciConnectionException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.CannotGetJdbcConnectionException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.PermissionDeniedDataAccessException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.UncategorizedDataAccessException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.orm.hibernate5.HibernateJdbcException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.orm.hibernate5.HibernateSystemException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.orm.jpa.JpaSystemException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.SQLWarningException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.UncategorizedSQLException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.InvalidDataAccessResourceUsageException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.BadSqlGrammarException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jca.cci.CciOperationNotSupportedException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.orm.hibernate5.HibernateQueryException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.IncorrectUpdateSemanticsDataAccessException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.InvalidResultSetAccessException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.jca.cci.RecordTypeNotSupportedException", NON_TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.TypeMismatchDataAccessException", NON_TRANSIENT_DATA_ACCESS_ERROR);

        // Recoverable Exception
        map.put("org.springframework.dao.RecoverableDataAccessException", DATA_ACCESS_ERROR);

        // Script Exceptions
        map.put("org.springframework.jdbc.datasource.init.ScriptException", DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.datasource.init.CannotReadScriptException", DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.datasource.init.ScriptParseException", DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.datasource.init.ScriptStatementFailedException", DATA_ACCESS_ERROR);
        map.put("org.springframework.jdbc.datasource.init.UncategorizedScriptException", DATA_ACCESS_ERROR);

        //  TransientDataAccessExceptions
        map.put("org.springframework.dao.TransientDataAccessException", TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.ConcurrencyFailureException", TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.OptimisticLockingFailureException", TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.PessimisticLockingFailureException", TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.CannotAcquireLockException", TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.CannotSerializeTransactionException", TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.DeadlockLoserDataAccessException", TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.QueryTimeoutException", TRANSIENT_DATA_ACCESS_ERROR);
        map.put("org.springframework.dao.TransientDataAccessResourceException", TRANSIENT_DATA_ACCESS_ERROR);

        return Collections.unmodifiableMap(map);
    }

    public static boolean isCategorized(Throwable exception) {
        return ErrorCategory.RUNTIME_EXCEPTION_CATEGORY_MAP.containsKey(exception.getClass().getName());
    }
    /**
     *
     * @param exception
     * @param integrationCategories
     * @return
     */
    public static String getCategory(Throwable exception, Set<String> integrationCategories) {
        if (isCategorized(exception)) {
            String category = ErrorCategory.RUNTIME_EXCEPTION_CATEGORY_MAP.get(exception.getClass().getName());
            if (integrationCategories.contains(category)) {
                return category;
            }
            while (hasParentCategory(category)) {
                category = getParentCategory(category);
                if (integrationCategories.contains(category)) {
                    return category;
                }
            }
        }
        return null;
    }

    private static boolean hasParentCategory(final String category) {
        return CATEGORY_HIERACHY_MAP.containsKey(category);
    }

    private static String getParentCategory(final String category) {
        return CATEGORY_HIERACHY_MAP.get(category);
    }

}
