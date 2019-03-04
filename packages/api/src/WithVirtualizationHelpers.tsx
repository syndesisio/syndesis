import * as React from 'react';
import { ApiContext, IApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export interface IWithVirtualizationHelpersChildrenProps {
  deleteVirtualization(virtualizationName: string): Promise<void>;
}

export interface IWithVirtualizationHelpersProps {
  children(props: IWithVirtualizationHelpersChildrenProps): any;
}

export class WithVirtualizationHelpersWrapped extends React.Component<
  IWithVirtualizationHelpersProps & IApiContext
> {
  constructor(props: IWithVirtualizationHelpersProps & IApiContext) {
    super(props);
    this.deleteVirtualization = this.deleteVirtualization.bind(this);
  }

  /**
   * Deletes the virtualization with the specified name.
   * @param virtualizationName the name of the virtualization being deleted
   */
  public async deleteVirtualization(virtualizationName: string): Promise<void> {
    const response = await callFetch({
      headers: {},
      method: 'DELETE',
      url: `${this.props.dvApiUri}workspace/dataservices/${virtualizationName}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

  public render() {
    return this.props.children({
      deleteVirtualization: this.deleteVirtualization,
    });
  }
}

export const WithVirtualizationHelpers: React.FunctionComponent<
  IWithVirtualizationHelpersProps
> = props => (
  <ApiContext.Consumer>
    {apiContext => (
      <WithVirtualizationHelpersWrapped {...props} {...apiContext} />
    )}
  </ApiContext.Consumer>
);
