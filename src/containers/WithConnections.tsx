import { Spinner } from 'patternfly-react';
import * as React from 'react';
import { RestError } from '../ui';
import { IConnection, SyndesisRest } from './index';

export interface IConnectionsRawResponse {
  items: IConnection[];
  totalCount: number;
}

export interface IConnectionsResponse {
  connections: IConnection[];
  connectionsCount: number;
}

export interface IWithConnectionsProps {
  children(props: IConnectionsResponse): any;
}

export class WithConnections extends React.Component<IWithConnectionsProps> {
  public render() {
    return (
      <SyndesisRest<IConnectionsRawResponse> url={'/api/v1/connections'} poll={5000}>
        {({loading, error, data}) => {
          if (loading) {
            return <Spinner/>;
          } else if (error) {
            return <RestError/>
          } else {
            return this.props.children({
              connections: data!.items,
              connectionsCount: data!.totalCount
            });
          }
        }}
      </SyndesisRest>
    )
  }
}