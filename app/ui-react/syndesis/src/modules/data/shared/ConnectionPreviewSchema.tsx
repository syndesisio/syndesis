import { SchemaNodeInfo } from '@syndesis/models';
import {
  PreviewListViewComponent,
  PreviewSelectedConnection,
} from '@syndesis/ui';
import * as React from 'react';
import { useTranslation } from 'react-i18next';

export interface IConnectionPreviewSchemaProps {
  selectedSchemaNodes: SchemaNodeInfo[];
}

export const ConnectionPreviewSchema: React.FunctionComponent<IConnectionPreviewSchemaProps> = props => {
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
    <PreviewSelectedConnection 
      selectedSchemaNodesLength={props.selectedSchemaNodes.length}
      i18nTablesSelected={t('shared:TablesSelected')}
      i18nEmptyTablePreview={t('shared:EmptyTablePreview')}
      >
        {props.selectedSchemaNodes.map((info, index) => (
          <PreviewListViewComponent
            key={index}
            name={info.name}
            index={index}
            toggle={toggle}
            expanded={expanded}
          />
        ))}
    </PreviewSelectedConnection>
  );
};
