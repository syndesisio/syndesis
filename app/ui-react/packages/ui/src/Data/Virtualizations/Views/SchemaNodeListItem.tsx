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

export interface ISchemaNodeListItemState {
  itemSelected: boolean;
}

export class SchemaNodeListItem extends React.Component<
  ISchemaNodeListItemProps,
  ISchemaNodeListItemState
> {
  public constructor(props: ISchemaNodeListItemProps) {
    super(props);
    this.state = {
      itemSelected: props.selected, // initial item selection
    };
    this.handleCheckboxToggle = this.handleCheckboxToggle.bind(this);
  }

  public handleCheckboxToggle = (connectionName: string, nodePath: string) => (
    event: any
  ) => {
    this.setState({
      itemSelected: !this.state.itemSelected,
    });
    this.props.onSelectionChanged(
      connectionName,
      nodePath,
      !this.state.itemSelected
    );
  };

  public schemaDisplayPath(schemaPath: string) {
    let result = '';
    schemaPath
      .split('/')
      .map(segment => (result += '/' + segment.split('=')[1]));
    return result;
  }

  public render() {
    return (
      <ListViewItem
        data-testid={`schema-node-list-item-${toValidHtmlId(
          this.props.name
        )}-list-item`}
        heading={this.props.name}
        className={'schema-node-list-item'}
        description={this.schemaDisplayPath(this.props.schemaPath)}
        checkboxInput={
          <input
            data-testid={'schema-node-list-item-selected-input'}
            type="checkbox"
            value=""
            defaultChecked={this.props.selected}
            onChange={this.handleCheckboxToggle(
              this.props.connectionName,
              this.props.schemaPath
            )}
          />
        }
        hideCloseIcon={true}
        stacked={false}
      />
    );
  }
}
