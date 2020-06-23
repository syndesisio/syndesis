import {
  ConnectionTable,
  SchemaNodeInfo,
  SourceColumn,
} from '@syndesis/models';
import {
  SelectedConnectionListView,
  SelectedConnectionTables,
} from '@syndesis/ui';
import * as React from 'react';
import { useTranslation } from 'react-i18next';

export interface IConnectionTablesProps {
  selectedSchemaNodes: SchemaNodeInfo[];
  columnDetails: ConnectionTable[];
  connectionIcons: Map<string, React.ReactNode>;
  onNodeDeselected: (connectionName: string, teiidName: string) => void;
  setShowPreviewData: (connectionName: string, tableName: string) => void;
}

export const ConnectionTables: React.FunctionComponent<IConnectionTablesProps> = props => {
  const { t } = useTranslation(['data', 'shared']);

  /* States used in component */
  const [expanded, setExpanded] = React.useState(['']);

  const toggle = (id: string) => {
    const newArray = expanded.slice();
    const index = newArray.indexOf(id);
    if (index >= 0) {
      newArray.splice(index, 1);
    } else {
      newArray.splice(newArray.length, 0, id);
    }
    setExpanded(newArray);
  };

  const getSelectedTableColumns = (
    connectionName: string,
    tableName: string
  ) => {
    let columnList: SourceColumn[] = [];
    for (const connection of props.columnDetails) {
      if (connection.name === connectionName) {
        for (const table of connection.tables) {
          if (table.name === tableName) {
            columnList = table.columns;
          }
        }
      }
    }
    return columnList.map((column: SourceColumn) => [
      column.name,
      column.datatype,
    ]);
  };

  return (
    <SelectedConnectionTables
      selectedSchemaNodesLength={props.selectedSchemaNodes.length}
      i18nTablesSelected={t('shared:TablesSelected')}
      i18nEmptyTablePreview={t('shared:EmptyTablePreview')}
      i18nEmptyTablePreviewTitle={t('shared:EmptyTablePreviewTitle')}
    >
      {props.selectedSchemaNodes.map((info, index) => (
        <SelectedConnectionListView
          key={index}
          name={info.teiidName}
          connectionIcon={props.connectionIcons.get(info.connectionName)}
          connectionName={info.connectionName}
          index={index}
          toggle={toggle}
          expanded={expanded}
          onTableRemoved={props.onNodeDeselected}
          setShowPreviewData={props.setShowPreviewData}
          i18nRemoveSelection={t('removeSelection')}
          i18nPreviewData={t('previewData')}
          rows={getSelectedTableColumns(info.connectionName, info.teiidName)}
        />
      ))}
    </SelectedConnectionTables>
  );
};
