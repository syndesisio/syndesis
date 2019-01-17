import { Extension } from '@syndesis/models';
import * as React from 'react';
import { IRestState } from './Rest';
import { ServerEventsContext } from './ServerEventsContext';
import { SyndesisRest } from './SyndesisRest';
import { WithChangeListener } from './WithChangeListener';
import { IChangeEvent } from './WithServerEvents';

export interface IExtensionsResponse {
  items: Extension[];
  totalCount: number;
}

export interface IWithExtensionsProps {
  disableUpdates?: boolean;
  children(props: IRestState<IExtensionsResponse>): any;
}

export class WithExtensions extends React.Component<IWithExtensionsProps> {
  public changeFilter(change: IChangeEvent) {
    return change.kind === 'extension';
  }

  public render() {
    return (
      <SyndesisRest<IExtensionsResponse>
        url={'/extensions'}
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
