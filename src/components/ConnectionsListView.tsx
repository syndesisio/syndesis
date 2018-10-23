import * as React from 'react';
import { Link } from 'react-router-dom';
import { IConnection } from '../containers';
import { ConnectionsGrid } from './ConnectionsGrid';
import { IListViewToolbarProps, ListViewToolbar } from './ListViewToolbar';

export interface IConnectionsListViewProps extends IListViewToolbarProps {
  match: any;
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
          <ConnectionsGrid connections={this.props.connections}/>
        </div>
      </>
    );
  }

}