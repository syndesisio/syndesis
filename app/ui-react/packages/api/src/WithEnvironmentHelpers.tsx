import * as React from 'react';
import { ApiContext, IApiContext } from './ApiContext';
import { callFetch } from './callFetch';
import { throwStandardError } from './helpers';

/**
 *
 */
export interface IWithEnvironmentHelpersChildrenProps {
  deleteEnvironment(name: string): Promise<any>;
  createEnvironment(name: string): Promise<any>;
  renameEnvironment(name: string, newName: string): Promise<any>;
}

export interface IWithEnvironmentHelpersProps {
  children(props: IWithEnvironmentHelpersChildrenProps): any;
}

export class WithEnvironmentHelpersWrapped extends React.Component<
  IWithEnvironmentHelpersProps & IApiContext
> {
  constructor(props: IWithEnvironmentHelpersProps & IApiContext) {
    super(props);
    this.createEnvironment = this.createEnvironment.bind(this);
    this.deleteEnvironment = this.deleteEnvironment.bind(this);
    this.renameEnvironment = this.renameEnvironment.bind(this);
  }

  public async createEnvironment(name: string) {
    const response = await callFetch({
      headers: this.props.headers,
      method: 'POST',
      url: `${this.props.apiUri}/public/environments/${name}`,
    });
    if (!response.ok) {
      await throwStandardError(response);
    }
    return response.body;
  }

  public async deleteEnvironment(name: string) {
    const response = await callFetch({
      headers: this.props.headers,
      method: 'DELETE',
      url: `${this.props.apiUri}/public/environments/${name}`,
    });
    if (!response.ok) {
      await throwStandardError(response);
    }
    return response.body;
  }

  public async renameEnvironment(name: string, newName: string) {
    const response = await callFetch({
      body: newName,
      headers: this.props.headers,
      method: 'PUT',
      stringifyBody: false,
      url: `${this.props.apiUri}/public/environments/${name}`,
    });
    if (!response.ok) {
      await throwStandardError(response);
    }
    return response.body;
  }

  public render() {
    return this.props.children({
      createEnvironment: this.createEnvironment,
      deleteEnvironment: this.deleteEnvironment,
      renameEnvironment: this.renameEnvironment,
    });
  }
}

export const WithEnvironmentHelpers: React.FunctionComponent<
  IWithEnvironmentHelpersProps
> = props => (
  <ApiContext.Consumer>
    {apiContext => <WithEnvironmentHelpersWrapped {...props} {...apiContext} />}
  </ApiContext.Consumer>
);
