import { IntegrationOverview } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisFetch } from './SyndesisFetch';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export interface IWithExtensionIntegrationsProps {
  extensionId: string;
  disableUpdates?: boolean;
  children(props: IFetchState<IntegrationOverview[]>): any;
}

/**
 * A component that fetches the integrations that a specified extension is used by.
 * @see [extensionId]{@link IWithExtensionIntegrationsProps#extensionId}
 */
export class WithExtensionIntegrations extends React.Component<
  IWithExtensionIntegrationsProps
> {
  public changeFilter(change: IChangeEvent) {
    return (
      // rerun fetch if there was a change in integrations
      change.kind === 'integration' || change.kind === 'integration-deployment'
    );
  }

  public render() {
    return (
      <SyndesisFetch<IntegrationOverview[]>
        url={`/extensions/${this.props.extensionId}/integrations`}
        defaultValue={[]}
      >
        {({ read, response }) =>
          this.props.disableUpdates ? (
            this.props.children(response)
          ) : (
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
          )
        }
      </SyndesisFetch>
    );
  }
}
