import { OAuthApp } from '@syndesis/models';
import * as React from 'react';
import { ApiContext, IApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export interface IWithOAuthAppHelpersChildrenProps {
  updateOAuthApp(app: OAuthApp): Promise<void>;
  deleteOAuthApp(name: string): Promise<void>;
}

export interface IWithOAuthAppHelpersProps {
  children(props: IWithOAuthAppHelpersChildrenProps): any;
}

export class WithOAuthAppHelpersWrapped extends React.Component<
  IWithOAuthAppHelpersProps & IApiContext
> {
  constructor(props: IWithOAuthAppHelpersProps & IApiContext) {
    super(props);
    this.updateOAuthApp = this.updateOAuthApp.bind(this);
    this.deleteOAuthApp = this.deleteOAuthApp.bind(this);
  }

  public async updateOAuthApp(app: OAuthApp) {
    const response = await callFetch({
      body: app,
      headers: this.props.headers,
      method: 'PUT',
      url: `${this.props.apiUri}/setup/oauth-apps/${app.id}`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }
  }

  public async deleteOAuthApp(id: string) {
    const response = await callFetch({
      headers: this.props.headers,
      method: 'DELETE',
      url: `${this.props.apiUri}/setup/oauth-apps/${id}`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }
  }

  public render() {
    return this.props.children({
      deleteOAuthApp: this.deleteOAuthApp,
      updateOAuthApp: this.updateOAuthApp,
    });
  }
}

export const WithOAuthAppHelpers: React.FunctionComponent<
  IWithOAuthAppHelpersProps
> = props => (
  <ApiContext.Consumer>
    {apiContext => <WithOAuthAppHelpersWrapped {...props} {...apiContext} />}
  </ApiContext.Consumer>
);
