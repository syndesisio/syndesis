import { SchemaNodeInfo } from '@syndesis/models';
import {
  SelectedConnectionListView,
  SelectedConnectionTabels,
} from '@syndesis/ui';
import * as React from 'react';
import { useTranslation } from 'react-i18next';

export interface IConnectionTablesProps {
  selectedSchemaNodes: SchemaNodeInfo[];
  onNodeDeselected: (connectionName: string, teiidName: string) => void;
}

export const ConnectionTables: React.FunctionComponent<IConnectionTablesProps> = props => {
  const [expanded, setExpanded] = React.useState(['']);
  const { t } = useTranslation(['data', 'shared']);
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

  return (
    <SelectedConnectionTabels 
      selectedSchemaNodesLength={props.selectedSchemaNodes.length}
      i18nTablesSelected={t('shared:TablesSelected')}
      i18nEmptyTablePreview={t('shared:EmptyTablePreview')}
      >
        {props.selectedSchemaNodes.map((info, index) => (
          <SelectedConnectionListView
            key={index}
            name={info.teiidName}
            connectionName={info.connectionName}
            index={index}
            toggle={toggle}
            expanded={expanded}
            onTabelRemoved={props.onNodeDeselected}
          />
        ))}
    </SelectedConnectionTabels>
  );
};
