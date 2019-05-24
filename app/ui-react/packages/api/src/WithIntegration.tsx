import { IIntegrationOverviewWithDraft } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisFetch } from './SyndesisFetch';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export interface IWithIntegrationProps {
  integrationId: string;
  initialValue?: IIntegrationOverviewWithDraft;
  disableUpdates?: boolean;
  children(props: IFetchState<IIntegrationOverviewWithDraft>): any;
}

/**
 * A component that fetches the integration with the specified identifier.
 * @see [integrationId]{@link IWithIntegrationProps#integrationId}
 */
export class WithIntegration extends React.Component<IWithIntegrationProps> {
  public constructor(props: IWithIntegrationProps) {
    super(props);
    this.changeFilter = this.changeFilter.bind(this);
  }
  public changeFilter(change: IChangeEvent) {
    return (
      change.kind.startsWith('integration') &&
      change.id.startsWith(this.props.integrationId)
    );
  }
  public render() {
    return (
      <SyndesisFetch<IIntegrationOverviewWithDraft>
        url={`/integrations/${this.props.integrationId}`}
        defaultValue={{
          isDraft: true,
          name: '',
        }}
        initialValue={this.props.initialValue}
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
