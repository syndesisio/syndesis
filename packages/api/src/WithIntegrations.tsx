import { IntegrationOverview } from '@syndesis/models';
import * as React from 'react';
import { IRestState } from './Rest';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisRest } from './SyndesisRest';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export interface IIntegrationsResponse {
  items: IntegrationOverview[];
  totalCount: number;
}

export interface IWithIntegrationsProps {
  disableUpdates?: boolean;
  children(props: IRestState<IIntegrationsResponse>): any;
}

export class WithIntegrations extends React.Component<IWithIntegrationsProps> {
  public changeFilter(change: IChangeEvent) {
    return (
      change.kind === 'integration' || change.kind === 'integration-deployment'
    );
  }

  public render() {
    return (
      <SyndesisRest<IIntegrationsResponse>
        url={'/integrations'}
        defaultValue={{ items: [], totalCount: 0 }}
      >
        {({ read, response }) =>
          this.props.disableUpdates ? (
            this.props.children(response)
          ) : (
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
          )
        }
      </SyndesisRest>
    );
  }
}
