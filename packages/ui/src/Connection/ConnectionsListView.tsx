import * as React from 'react';
import { Link } from 'react-router-dom';
import { IListViewToolbarProps, ListViewToolbar } from '../Shared';

export interface IConnectionsListViewProps extends IListViewToolbarProps {
  linkToConnectionCreate: string;
  i18nLinkCreateConnection: string;
}

export class ConnectionsListView extends React.Component<
  IConnectionsListViewProps
> {
  public render() {
    return (
      <>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <Link
              to={this.props.linkToConnectionCreate}
              className={'btn btn-primary'}
            >
              {this.props.i18nLinkCreateConnection}
            </Link>
          </div>
        </ListViewToolbar>
        <div className={'container-fluid'}>{this.props.children}</div>
      </>
    );
  }
}
