// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import {
  DataListCell,
  DataListCheck,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
} from '@patternfly/react-core';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';

import './SchemaNodeListItem.css';

export interface ISchemaNodeListItemProps {
  name: string;
  connectionIcon?: JSX.Element;
  connectionName: string;
  isVirtualizationSchema: boolean;
  teiidName: string;
  nodePath: string[];
  selected: boolean;
  onSelectionChanged: (
    connectionName: string,
    isVirtualizationSchema: boolean,
    name: string,
    teiidName: string,
    nodePath: string[],
    selected: boolean,
    connectionIcon?: JSX.Element
  ) => void;
}

export const SchemaNodeListItem: React.FunctionComponent<
  ISchemaNodeListItemProps
> = props => {

  const [itemSelected, setItemSelected] = React.useState(props.selected);
  React.useEffect(() => {
    setItemSelected(props.selected);
  }, [props.selected]);

  const doToggleCheckbox = (connectionName: string, isVirtualizationSchema: boolean, name: string, teiidName: string, nodePath: string[], connectionIcon?: JSX.Element) => (
    event: any
  ) => {
    setItemSelected(!itemSelected);

    props.onSelectionChanged(
      connectionName,
      isVirtualizationSchema,
      name,
      teiidName,
      nodePath,
      !itemSelected,
      connectionIcon
    );
  };

  const schemaDisplayPath = (nodePath: string[]) => {
    let result = '';
    nodePath.map(segment => (result += '/' + segment));
    return result;
  }
  
  return (
    <DataListItem
      aria-labelledby={'schema node list item'}
      data-testid={`schema-node-list-item-${toValidHtmlId(
        props.name
      )}-list-item`}
      className={'schema-node-list-item'}
    >
      <DataListItemRow>
        <DataListCheck
          aria-labelledby="schema-node-list-item-check"
          name="schema-node-list-item-check"
          checked={props.selected}
          onChange={doToggleCheckbox(
            props.connectionName,
            props.isVirtualizationSchema,
            props.name,
            props.teiidName,
            props.nodePath,
            props.connectionIcon
          )}
        />
        <DataListItemCells
          dataListCells={[
            <DataListCell key={'primary content'} width={2}>
              <div className={'schema-node-list-item__text-wrapper'}>
                <b>{props.name}</b>
              </div>
            </DataListCell>,
            <DataListCell key={'secondary content'} width={2}>
              <div className={'schema-node-list-item__path'}>
                {schemaDisplayPath(props.nodePath)}
              </div>
            </DataListCell>,
          ]}
        />
      </DataListItemRow>
    </DataListItem>
  );
}
