import { ListView, ListViewIcon, ListViewItem } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';

export interface IConnectionSchemaListItemProps {
  icon?: string;
  connectionName: string;
  connectionDescription: string;
}

export class ConnectionSchemaListItem extends React.Component<
  IConnectionSchemaListItemProps
> {
  public render() {
    return (
      <>
        <ListViewItem
          data-testid={`connection-schema-list-item-${toValidHtmlId(
            this.props.connectionName
          )}-list-item`}
          heading={this.props.connectionName}
          description={
            this.props.connectionDescription
              ? this.props.connectionDescription
              : ''
          }
          hideCloseIcon={true}
          leftContent={
            this.props.icon ? (
              <div className="blank-slate-pf-icon">
                <img
                  src={this.props.icon}
                  alt={this.props.connectionName}
                  width={46}
                />
              </div>
            ) : (
              <ListViewIcon name={'database'} />
            )
          }
          stacked={true}
        >
          {this.props.children ? (
            <ListView>{this.props.children}</ListView>
          ) : null}
        </ListViewItem>
      </>
    );
  }
}
