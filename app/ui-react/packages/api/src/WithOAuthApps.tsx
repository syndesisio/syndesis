import { IListResult, OAuthApp } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';
import { WithPolling } from './WithPolling';

export interface IWithOAuthAppsRenderProps
  extends IFetchState<IListResult<OAuthApp>> {
  read(): Promise<void>;
}

export interface IWithOAuthAppsProps {
  disableUpdates?: boolean;
  children(props: IWithOAuthAppsRenderProps): any;
}

export class WithOAuthApps extends React.Component<IWithOAuthAppsProps> {
  public render() {
    return (
      <SyndesisFetch<IListResult<OAuthApp>>
        url={'/setup/oauth-apps?per_page=50'}
        defaultValue={{
          items: [],
          totalCount: 0,
        }}
      >
        {({ read, response }) => {
          if (this.props.disableUpdates) {
            return this.props.children({ ...response, read });
          }
          return (
            <WithPolling read={read} polling={5000}>
              {() => this.props.children({ ...response, read })}
            </WithPolling>
          );
        }}
      </SyndesisFetch>
    );
  }
}
