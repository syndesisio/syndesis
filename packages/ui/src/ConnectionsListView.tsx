import * as React from 'react';
import { Link } from 'react-router-dom';
import { IListViewToolbarProps, ListViewToolbar } from './ListViewToolbar';

export interface IConnectionsListViewProps extends IListViewToolbarProps {
  linkToConnectionCreate: string;
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
              Create Connection
            </Link>
          </div>
        </ListViewToolbar>
        <div className={'container-fluid'}>{this.props.children}</div>
      </>
    );
  }
}
