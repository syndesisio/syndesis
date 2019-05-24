import { Connector } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisFetch } from './SyndesisFetch';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export interface IApiConnectorsResponse {
  items: Connector[];
  totalCount: number;
}

export interface IWithApiConnectorsProps {
  disableUpdates?: boolean;
  children(props: IFetchState<IApiConnectorsResponse>): any;
}

export class WithApiConnectors extends React.Component<
  IWithApiConnectorsProps
> {
  public changeFilter(change: IChangeEvent) {
    return change.kind.startsWith('connector');
  }

  public render() {
    return (
      <SyndesisFetch<IApiConnectorsResponse>
        url={
          '/connectors?query=connectorGroupId%3Dswagger-connector-template&per_page=50'
        }
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
              {({
                registerChangeListener,
                unregisterChangeListener,
                registerMessageListener,
                unregisterMessageListener,
              }) => (
                <WithChangeListener
                  read={read}
                  registerChangeListener={registerChangeListener}
                  unregisterChangeListener={unregisterChangeListener}
                  registerMessageListener={registerMessageListener}
                  unregisterMessageListener={unregisterMessageListener}
                  filter={this.changeFilter}
                >
                  {() => this.props.children(response)}
                </WithChangeListener>
              )}
            </ServerEventsContext.Consumer>
          );
        }}
      </SyndesisFetch>
    );
  }
}
