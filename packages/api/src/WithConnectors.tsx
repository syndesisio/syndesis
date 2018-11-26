import { Connector } from '@syndesis/models';
import * as React from 'react';
import { IRestState } from './Rest';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisRest } from './SyndesisRest';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export interface IConnectorsResponse {
  items: Connector[];
  totalCount: number;
}

export interface IWithConnectorsProps {
  disableUpdates?: boolean;
  children(props: IRestState<IConnectorsResponse>): any;
}

export class WithConnectors extends React.Component<IWithConnectorsProps> {
  public changeFilter(change: IChangeEvent) {
    return change.kind.startsWith('connector');
  }
  public render() {
    return (
      <SyndesisRest<IConnectorsResponse>
        url={'/connectors'}
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
