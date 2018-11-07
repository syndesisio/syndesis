import * as React from 'react';
import { IConnection, IRestState, SyndesisRest } from './index';

export interface IConnectionsResponse {
  items: IConnection[];
  totalCount: number;
}

export interface IWithConnectionsProps {
  children(props: IRestState<IConnectionsResponse>): any;
}

export class WithConnections extends React.Component<IWithConnectionsProps> {
  public render() {
    return (
      <SyndesisRest<IConnectionsResponse>
        url={'/api/v1/connections'}
        poll={5000}
        defaultValue={{
          items: [],
          totalCount: 0
        }}
      >
        {response => this.props.children(response)}
      </SyndesisRest>
    );
  }
}
