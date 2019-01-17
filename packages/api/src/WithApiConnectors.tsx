import { Connector } from '@syndesis/models';
import * as React from 'react';
import { IRestState } from './Rest';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisRest } from './SyndesisRest';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export interface IApiConnectorsResponse {
  items: Connector[];
  totalCount: number;
}

export interface IWithApiConnectorsProps {
  disableUpdates?: boolean;
  children(props: IRestState<IApiConnectorsResponse>): any;
}

export class WithApiConnectors extends React.Component<
  IWithApiConnectorsProps
> {
  public changeFilter(change: IChangeEvent) {
    return change.kind.startsWith('apiConnector');
  }

  public render() {
    return (
      <SyndesisRest<IApiConnectorsResponse>
        url={'/connectors?query=connectorGroupId%3Dswagger-connector-template'}
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
