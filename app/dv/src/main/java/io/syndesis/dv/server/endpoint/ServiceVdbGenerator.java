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
package io.syndesis.dv.server.endpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.data.util.Pair;
import org.teiid.adminapi.Admin.SchemaObjectType;
import org.teiid.adminapi.DataPolicy.ResourceType;
import org.teiid.adminapi.impl.DataPolicyMetadata;
import org.teiid.adminapi.impl.DataPolicyMetadata.PermissionMetaData;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.VDBImportMetadata;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.language.SQLConstants;
import org.teiid.metadata.AbstractMetadataRecord;
import org.teiid.metadata.Column;
import org.teiid.metadata.KeyRecord;
import org.teiid.metadata.KeyRecord.Type;
import org.teiid.metadata.Schema;
import org.teiid.metadata.Table;
import org.teiid.query.metadata.DDLConstants;
import org.teiid.query.metadata.DDLStringVisitor;
import org.teiid.query.sql.visitor.SQLStringVisitor;

import io.syndesis.dv.KException;
import io.syndesis.dv.StringConstants;
import io.syndesis.dv.metadata.TeiidDataSource;
import io.syndesis.dv.metadata.TeiidVdb;
import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.model.TablePrivileges;
import io.syndesis.dv.model.TablePrivileges.Privilege;
import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.utils.PathUtils;

/**
 * This class provides methods to generate data service vdbs containing a view model
 * and one or more source models
 *
 * Each model is created via generating DDL and calling setModelDefinition() method
 *
 */
public final class ServiceVdbGenerator implements StringConstants {

    public static final String ANY_AUTHENTICATED = "any authenticated"; //$NON-NLS-1$

    public interface SchemaFinder {
        Schema findSchema(String connectionName) throws KException;

        TeiidDataSource findTeiidDatasource(String connectionName) throws KException;
    }

    private static final String COMMENT_START = "/*"; //$NON-NLS-1$
    private static final String COMMENT_END = "*/"; //$NON-NLS-1$
    /**
     * Inner Join Type
     */
    public static final String JOIN_INNER = "INNER_JOIN"; //$NON-NLS-1$
    /**
     * Left Outer Join type
     */
    public static final String JOIN_LEFT_OUTER = "LEFT_OUTER_JOIN"; //$NON-NLS-1$
    /**
     * Right Outer Join type
     */
    public static final String JOIN_RIGHT_OUTER = "RIGHT_OUTER_JOIN"; //$NON-NLS-1$
    /**
     * Full Outer Join type
     */
    public static final String JOIN_FULL_OUTER = "FULL_OUTER_JOIN"; //$NON-NLS-1$

    private final SchemaFinder finder;

    /**
     * Constructs a ServiceVdbGenerator instance
     *
     * @param finder
     */
    public ServiceVdbGenerator( final SchemaFinder finder ) {
        this.finder = finder;
    }

    /**
     * Creates the service vdb - must be valid
     * @throws KException
     */
    public VDBMetaData createServiceVdb(String virtualizationName, TeiidVdb previewVDB, List<? extends ViewDefinition> editorStates, List<TablePrivileges> tablePrivileges) throws KException {
        VDBMetaData vdb = new VDBMetaData();
        vdb.addProperty("hidden-qualified", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        vdb.setName(virtualizationName);
        // Keep track of unique list of sources needed
        Map< Schema, LinkedHashSet<Table> > schemaTableMap = new LinkedHashMap<Schema, LinkedHashSet<Table>>();

        // Generate new model DDL by appending all view DDLs
        StringBuilder allViewDdl = new StringBuilder();

        Map<String, String> viewMap = new HashMap<>();

        Schema s = previewVDB.getSchema(virtualizationName);

        for ( final ViewDefinition viewDef : editorStates ) {
            if( !viewDef.isComplete()) {
                continue;
            }

            viewMap.put(viewDef.getId(), viewDef.getName());

            String viewDdl = viewDef.getDdl();
            allViewDdl.append(viewDdl);
            if (!viewDdl.endsWith(SEMI_COLON)) {
                allViewDdl.append(SEMI_COLON);
            }
            allViewDdl.append(NEW_LINE);

            AbstractMetadataRecord record = s.getTable(viewDef.getName());

            for( AbstractMetadataRecord info : record.getIncomingObjects()) {
                if (!(info instanceof Table)) {
                    continue;
                }
                Table tbl = (Table)info;
                Schema schemaModel = tbl.getParent();
                if (!schemaModel.isPhysical() || tbl.isSystem()) {
                    continue;
                }
                LinkedHashSet<Table> tbls = schemaTableMap.get(schemaModel);
                if (!schemaTableMap.containsKey(schemaModel)) {
                    tbls = new LinkedHashSet<Table>();
                    schemaTableMap.put(schemaModel, tbls);
                }
                tbls.add(tbl);
            }
        }

        addServiceModel(virtualizationName, vdb, allViewDdl);

        // Iterate each schemaModel, generating a source for it.
        for ( Map.Entry<Schema, LinkedHashSet<Table>> entry: schemaTableMap.entrySet() ) {
            // Iterate tables for this schema, generating DDL
            String connectionName = entry.getKey().getName();

            StringBuilder regex = new StringBuilder();
            for ( Table table: entry.getValue() ) {
                if (regex.length() > 0) {
                    regex.append("|"); //$NON-NLS-1$
                }
                regex.append(Pattern.quote(table.getName()));
            }

            String ddl = DDLStringVisitor.getDDLString(entry.getKey(), EnumSet.of(SchemaObjectType.TABLES), regex.toString());

            // Create a source model and set the DDL string via setModelDeinition(DDL)
            ModelMetaData srcModel = new ModelMetaData();
            srcModel.setVisible(false);
            srcModel.setName(entry.getKey().getName());
            vdb.addModel(srcModel);
            srcModel.setModelType(org.teiid.adminapi.Model.Type.PHYSICAL);
            srcModel.addSourceMetadata("DDL", ddl); //$NON-NLS-1$

            // Add ModelSource based on currentSchemaModel ModelSource info
            TeiidDataSource tds = finder.findTeiidDatasource(connectionName);

            if (tds != null) {
                // add the source mapping
                srcModel.addSourceMapping(tds.getName(), tds.getTranslatorName(), tds.getName());
            }
        }

        addRoleInformation(vdb, tablePrivileges, viewMap);

        return vdb;

    }

    private void addRoleInformation(VDBMetaData vdb, List<TablePrivileges> tablePrivileges,
            Map<String, String> viewMap) {
        if (tablePrivileges == null) {
            return;
        }
        //group by role - we're assuming case-sensitive names
        Map<String, List<TablePrivileges>> roleMap = new HashMap<String, List<TablePrivileges>>();
        for (TablePrivileges privileges : tablePrivileges) {
            roleMap.computeIfAbsent(privileges.getRoleName(), (k)->new ArrayList<>()).add(privileges);
        }
        for (Map.Entry<String, List<TablePrivileges>> entry : roleMap.entrySet()) {
            String roleName = entry.getKey();
            DataPolicyMetadata dpm = new DataPolicyMetadata();
            dpm.setName(roleName);
            if (roleName.equals(ANY_AUTHENTICATED)) {
                dpm.setAnyAuthenticated(true);
            } else {
                dpm.setMappedRoleNames(Arrays.asList(roleName));
            }
            for (TablePrivileges privileges : entry.getValue()) {
                String tableName = viewMap.get(privileges.getViewDefinitionId());
                if (tableName == null) {
                    continue; // not included
                }
                PermissionMetaData pmd = new PermissionMetaData();
                //add schema qualification
                pmd.setResourceName(vdb.getName() + "." + tableName); //$NON-NLS-1$
                pmd.setResourceType(ResourceType.TABLE);
                for (Privilege p : privileges.getGrantPrivileges()) {
                    switch (p) {
                    case D:
                        pmd.setAllowDelete(true);
                        break;
                    case I:
                        pmd.setAllowCreate(true);
                        break;
                    case S:
                        pmd.setAllowRead(true);
                        break;
                    case U:
                        pmd.setAllowUpdate(true);
                        break;
                    }
                }
                dpm.addPermission(pmd);
            }
            //even if it's empty, we can still add it - for use by hasRole
            vdb.addDataPolicy(dpm);
        }
    }

    /**
     * This method creates a preview vdb, that includes all parsable sql and imports the base
     * preview vdb.  It is not guaranteed to be valid.
     */
    public VDBMetaData createPreviewVdb(String virtualizationName, String vdbName, List<? extends ViewDefinition> editorStates) {
        VDBMetaData vdb = new VDBMetaData();
        vdb.addProperty("hidden-qualified", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        vdb.setName(vdbName);

        // Generate new model DDL by appending all view DDLs
        StringBuilder allViewDdl = new StringBuilder();

        for ( final ViewDefinition viewDef : editorStates ) {
            if(!viewDef.isComplete() || !viewDef.isParsable()) {
                continue;
            }

            String viewDdl = viewDef.getDdl();
            //we don't need an exhaustive check here,
            //the parser is tolerant to redundant semi-colons
            if (!viewDdl.endsWith(SEMI_COLON)) {
                allViewDdl.append(SEMI_COLON);
            }
            allViewDdl.append(viewDdl).append(NEW_LINE);
        }

        addServiceModel(virtualizationName, vdb, allViewDdl);

        VDBImportMetadata vdbImport = new VDBImportMetadata();
        vdbImport.setVersion(DefaultMetadataInstance.DEFAULT_VDB_VERSION);
        vdbImport.setName(EditorService.PREVIEW_VDB);
        //this makes the vdb tolerant to resolve/validation issue
        //so that as long as we parse, we'll load
        vdb.addProperty("preview", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        vdb.getVDBImports().add(vdbImport);
        return vdb;
    }

    private ModelMetaData addServiceModel(String virtualizationName, VDBMetaData vdb, StringBuilder allViewDdl) {
        ModelMetaData model = new ModelMetaData();
        model.setName(virtualizationName);
        model.setModelType(org.teiid.adminapi.Model.Type.VIRTUAL);
        vdb.addModel(model);
        model.addSourceMetadata("DDL", allViewDdl.toString()); //$NON-NLS-1$
        return model;
    }


    /*
     * Generates DDL for a view definition based on properties and supplied array of TableInfo from one or more sources
     */
    private String getODataViewDdl(ViewDefinition viewDef, TableInfo[] sourceTableInfos) {

        String viewName = viewDef.getName();

        StringBuilder sb = new StringBuilder();

        // Generate the View DDL
        startView(viewName, sb);
        sb.append(OPEN_BRACKET);
        sb.append(NEW_LINE);
        sb.append(SPACE);
        sb.append(SPACE);

        // add primary table
        appendViewColumns(sourceTableInfos[0], false, false, sb);

        // add all others in comments
        if (sourceTableInfos.length > 1) {
            for (int i = 1; i < sourceTableInfos.length; i++) {
                sb.append(NEW_LINE);
                sb.append(SPACE);
                sb.append(SPACE);
                sb.append(COMMENT_START);
                sb.append(StringConstants.COMMA);
                appendViewColumns(sourceTableInfos[i], false, false, sb);
                sb.append(COMMENT_END);
            }
        }

        // Add PK from primary
        KeyRecord constraint = sourceTableInfos[0].getUniqueConstraint();
        if (constraint != null) {
        	sb.append(COMMA).append(SPACE);
        	if (sourceTableInfos.length > 1) {
                sb.append(NEW_LINE);
                sb.append(SPACE);
                sb.append(SPACE);
        	}
            List<Column> keyCols = constraint.getColumns();
            sb.append(constraint.getType() == Type.Primary ? DDLConstants.PRIMARY_KEY : SQLConstants.Reserved.UNIQUE)
                    .append(StringConstants.OPEN_BRACKET);
            for (int i = 0; i < keyCols.size(); i++) {
                if (i > 0) {
                    sb.append(COMMA).append(SPACE);
                }
                sb.append(keyCols.get(i).getName());
            }
            sb.append(StringConstants.CLOSE_BRACKET);
        }

        sb.append(NEW_LINE).append(StringConstants.CLOSE_BRACKET).append(SPACE);
        sb.append(getTableAnnotation(viewDef.getDescription()));
        sb.append("AS \n  SELECT "); //$NON-NLS-1$

        sb.append(NEW_LINE);
        sb.append(SPACE).append(SPACE).append(SPACE).append(SPACE);
        appendViewColumns(sourceTableInfos[0], true, false, sb);
        if (sourceTableInfos.length > 1) {
            for (int i = 1; i < sourceTableInfos.length; i++) {
                sb.append(NEW_LINE);
                sb.append(SPACE).append(SPACE).append(SPACE).append(SPACE);
                sb.append(COMMENT_START);
                sb.append(COMMA);
                appendViewColumns(sourceTableInfos[i], true, false, sb);
                sb.append(COMMENT_END);
            }
        }

        sb.append(NEW_LINE); //$NON-NLS-1$
        sb.append("  FROM "); //$NON-NLS-1$
        sb.append(NEW_LINE);
        sb.append(SPACE).append(SPACE).append(SPACE).append(SPACE);

        for (int i = 0; i < sourceTableInfos.length; i++) {
            if (i == 0) {
                sb.append(sourceTableInfos[0].getFQName() + " AS " + sourceTableInfos[0].getAlias());
            } else {
                sb.append(NEW_LINE);
                sb.append(SPACE).append(SPACE).append(SPACE).append(SPACE);
                sb.append(COMMENT_START);
                sb.append(COMMA);
                if (constraint != null) {
                    sb.append(" [INNER|LEFT OUTER|RIGHT OUTER] JOIN ");
                    sb.append(sourceTableInfos[i].getFQName() + " AS " + sourceTableInfos[i].getAlias());
                    sb.append(" ON ");
                    List<Column> keyCols = constraint.getColumns();
                    for (int k = 0; k < keyCols.size(); k++) {
                        if (k > 0) {
                            sb.append(" AND ");
                        }
                        sb.append(sourceTableInfos[0].getAlias()).append(DOT)
                                .append(keyCols.get(k).getName()).append(EQUALS)
                                .append(sourceTableInfos[i].getAlias()).append(DOT)
                                .append("<?>");
                    }
                }
                sb.append(COMMENT_END);
            }
        }
        return sb.toString();
    }

    private void appendViewColumns(TableInfo tableInfo, boolean useAliased, boolean useType, StringBuilder sb) {
        for( Iterator<ColumnInfo> iter = tableInfo.getColumnInfos().iterator(); iter.hasNext();) {
            ColumnInfo info = iter.next();
            if (useAliased) {
                sb.append(info.getAliasedName());
            } else {
                sb.append(info.getName());
            }
            if (useType) {
                sb.append(SPACE);
                sb.append(info.getType());
            }
            if (iter.hasNext()) {
                sb.append(COMMA).append(SPACE);
            }
        }
    }

    private void startView(String viewName, StringBuilder sb) {
        sb.append("CREATE VIEW "); //$NON-NLS-1$
        sb.append(SQLStringVisitor.escapeSinglePart(viewName));
        sb.append(SPACE);
    }

    /**
     * Public method to generate the view DDL for a view definition
     *
     * @param viewDef
     *         the view definition
     * @return the View DDL
     * @throws KException
     *         if problem occurs
     */
    public String getODataViewDdl(ViewDefinition viewDef) throws KException {
        if ( !viewDef.isComplete() ) {
            return null;
        }
        TableInfo[] tableInfos = getSourceTableInfos(viewDef);
        if (tableInfos == null) {
            throw new KException("Error getting the ViewDefinition sources"); //$NON-NLS-1$
        }

        if (tableInfos.length == 0) {
            StringBuilder sb = new StringBuilder();
            startView(viewDef.getName(), sb);
            sb.append(getTableAnnotation(viewDef.getDescription()));
            sb.append("AS \nSELECT 1 as col;"); //$NON-NLS-1$
            return sb.toString();
        }

        return getODataViewDdl(viewDef, tableInfos);
    }

    /**
     * Generate the table annotation for the supplied description
     * @param description the description
     * @return the table annotation
     */
    private String getTableAnnotation(final String description) {
        if( description!=null && description.length()>0 ) {
            return "OPTIONS (ANNOTATION '" + description + "') ";
        }
        return "";
    }

    /*
     * Find and resolve source {@link TableInfo}s for a {@link ViewDefinition} object
     * @param uow
     * @param viewDefinition
     * @return {@link TableInfo} array
     * @throws KException
     */
    private TableInfo[] getSourceTableInfos(ViewDefinition viewDefinition) throws KException {
        if ( !viewDefinition.isComplete() ) {
            return null;
        }
        List<String> sourceTablePaths = viewDefinition.getSourcePaths();
        ArrayList<TableInfo> sourceTableInfos = new ArrayList<TableInfo>(sourceTablePaths.size());

        // Find and create TableInfo for each source Path
        for(String path : sourceTablePaths) {
            List<Pair<String, String>> options = PathUtils.getOptions(path);

            //format is connection=x/table=y
            //NOTE: will eventually need to accomodate other object types, like
            //procedures

            String connectionName = options.get(0).getSecond();

            // Find schema model based on the connection name (i.e. connection=pgConn)
            final Schema schemaModel = finder.findSchema(connectionName);

            // Get the tables from the schema and match them with the table name
            if ( schemaModel == null ) {
                return null;
            }
            String tableName = options.get(1).getSecond();

            Table table = schemaModel.getTable(tableName);
            if (table == null) {
                return null;
            }
            // create a new TableInfo object
            sourceTableInfos.add(new TableInfo(path, table, "t" + (sourceTableInfos.size()+1)));
        }

        return sourceTableInfos.toArray(new TableInfo[0]);
    }

    /*
     * Inner class to hold state for source table information and simplifies the DDL generating process
     */
    class TableInfo {
        private final String path;
        private final String alias;
        private final Table table;

        private String name;
        private String fqname;

        private List<ColumnInfo> columnInfos = new ArrayList<ColumnInfo>();

        private KeyRecord constraint;

        private TableInfo(String path, Table table, String alias) throws KException {
            this.path = path;
            this.alias = alias;
            this.table = table;
            this.name = SQLStringVisitor.escapeSinglePart(table.getName());
            Schema schemaModel = table.getParent();
            this.fqname = SQLStringVisitor.escapeSinglePart(schemaModel.getName()) + DOT + this.name;
            createColumnInfos(table);
            constraint = table.getPrimaryKey();
            if (constraint == null) {
                List<KeyRecord> unique = table.getUniqueKeys();
                if (!unique.isEmpty()) {
                    constraint = unique.get(0);
                }
            }
        }

        private void createColumnInfos(Table table) throws KException {
            // Walk through the columns and create an array of column + datatype strings
            List<Column> cols = table.getColumns();
            for( Column col : cols) {
                this.columnInfos.add(new ColumnInfo(col, getFQName(), this.alias));
            }
        }

        public String getSourceTablePath() {
            return this.path;
        }

        public String getName() {
            return this.name;
        }

        public Table getTable() {
            return this.table;
        }

        public String getAlias() {
            return this.alias;
        }

        public String getFQName() {
            return fqname;
        }

        public List<ColumnInfo> getColumnInfos() {
            return columnInfos;
        }

        public KeyRecord getUniqueConstraint() {
            return constraint;
        }
    }

    /*
     * Inner class to hold state for table column information and simplifies the DDL generating process
     */
    class ColumnInfo {
        private String name;
        private String fqname;
        private String aliasedName;
        private String type;

        private ColumnInfo(Column column, String tableFqn, String tblAlias) throws KException {
            this.name = SQLStringVisitor.escapeSinglePart(column.getName());
            this.aliasedName = name;
            if( tblAlias != null ) {
                this.aliasedName = tblAlias + DOT + name;
            }
            this.fqname = tableFqn + DOT + name;
            this.type = column.getDatatype().getName();
        }

        public String getName() {
            return this.name;
        }

        public String getAliasedName() {
            return this.aliasedName;
        }

        public String getFqname() {
            return this.fqname;
        }

        public String getType() {
            return this.type;
        }
    }
}
