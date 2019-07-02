// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import { ListViewItem } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';

import './SchemaNodeListItem.css';

export interface ISchemaNodeListItemProps {
  name: string;
  connectionName: string;
  schemaPath: string;
  selected: boolean;
  onSelectionChanged: (
    connectionName: string,
    nodePath: string,
    selected: boolean
  ) => void;
}

export const SchemaNodeListItem: React.FunctionComponent<
  ISchemaNodeListItemProps
> = props => {

  const [itemSelected, setItemSelected] = React.useState(props.selected);

  const doToggleCheckbox = (connectionName: string, nodePath: string) => (
    event: any
  ) => {
    setItemSelected(!itemSelected);

    props.onSelectionChanged(
      connectionName,
      nodePath,
      !itemSelected
    );
  };

  const schemaDisplayPath = (schemaPath: string) => {
    let result = '';
    schemaPath
      .split('/')
      .map(segment => (result += '/' + segment.split('=')[1]));
    return result;
  }

  return (
    <ListViewItem
      data-testid={`schema-node-list-item-${toValidHtmlId(
        props.name
      )}-list-item`}
      heading={props.name}
      className={'schema-node-list-item'}
      description={schemaDisplayPath(props.schemaPath)}
      checkboxInput={
        <input
          data-testid={'schema-node-list-item-selected-input'}
          type="checkbox"
          value=""
          defaultChecked={props.selected}
          onChange={doToggleCheckbox(
            props.connectionName,
            props.schemaPath
          )}
        />
      }
      hideCloseIcon={true}
      stacked={false}
    />
  );
}
