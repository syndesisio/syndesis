import { ConnectionOverview } from '@syndesis/models';
import * as React from 'react';
import { IRestState } from './Rest';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisRest } from './SyndesisRest';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export interface IConnectionsResponse {
  items: ConnectionOverview[];
  totalCount: number;
}

export interface IWithConnectionsProps {
  disableUpdates?: boolean;
  children(props: IRestState<IConnectionsResponse>): any;
}

export class WithConnections extends React.Component<IWithConnectionsProps> {
  public changeFilter(change: IChangeEvent) {
    return change.kind.startsWith('connection');
  }

  public render() {
    return (
      <SyndesisRest<IConnectionsResponse>
        url={'/connections'}
        defaultValue={{
          items: [],
          totalCount: 0,
        }}
      >
        {({ read, response }) => {
          if (this.props.disableUpdates) {
            return this.props.children(response);
          }
          return (
            <ServerEventsContext.Consumer>
              {({ registerChangeListener, unregisterChangeListener }) => (
                <WithChangeListener
                  read={read}
                  registerChangeListener={registerChangeListener}
                  unregisterChangeListener={unregisterChangeListener}
                  filter={this.changeFilter}
                >
                  {() => this.props.children(response)}
                </WithChangeListener>
              )}
            </ServerEventsContext.Consumer>
          );
        }}
      </SyndesisRest>
    );
  }
}
