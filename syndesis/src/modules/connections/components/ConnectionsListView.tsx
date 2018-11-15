import { Connection } from '@syndesis/models';
import {
  ConnectionCard,
  ConnectionsGrid,
  IListViewToolbarProps,
  ListViewToolbar,
} from '@syndesis/ui';
import { getConnectionIcon } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IConnectionsListViewProps extends IListViewToolbarProps {
  baseurl: string;
  loading: boolean;
  connections: Connection[];
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
              to={`${this.props.baseurl}/create`}
              className={'btn btn-primary'}
            >
              Create Connection
            </Link>
          </div>
        </ListViewToolbar>
        <div className={'container-fluid'}>
          <ConnectionsGrid loading={this.props.loading}>
            {this.props.connections.map((c, index) => (
              <ConnectionCard
                name={c.name}
                description={c.description || ''}
                icon={getConnectionIcon(c, process.env.PUBLIC_URL)}
                key={index}
              />
            ))}
          </ConnectionsGrid>
        </div>
      </>
    );
  }
}
