import { Extension } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisFetch } from './SyndesisFetch';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export interface IExtensionsResponse {
  items: Extension[];
  totalCount: number;
}

export interface IWithExtensionsProps {
  disableUpdates?: boolean;
  children(props: IFetchState<IExtensionsResponse>): any;
}

/**
 * A component that fetches all the extensions.
 */
export class WithExtensions extends React.Component<IWithExtensionsProps> {
  public changeFilter(change: IChangeEvent) {
    return change.kind === 'extension';
  }

  public render() {
    return (
      <SyndesisFetch<IExtensionsResponse>
        url={'/extensions?per_page=50'}
        defaultValue={{ items: [], totalCount: 0 }}
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
