import { IConnection } from '@syndesis/ui/containers';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { ConnectionsGrid } from './ConnectionsGrid';
import { IListViewToolbarProps, ListViewToolbar } from './ListViewToolbar';

export interface IConnectionsListViewProps extends IListViewToolbarProps {
  match: any;
  loading: boolean;
  connections: IConnection[];
}

export class ConnectionsListView extends React.Component<IConnectionsListViewProps> {
  public render() {
    return (
      <>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <Link
              to={`${this.props.match.url}/new`}
              className={'btn btn-primary'}
            >
              Create Connection
            </Link>
          </div>
        </ListViewToolbar>
        <div className={'container-fluid'}>
          <ConnectionsGrid
            loading={this.props.loading}
            connections={this.props.connections}
          />
        </div>
      </>
    );
  }

}