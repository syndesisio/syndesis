import * as React from 'react';
import { ApiContext, IApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export interface IWithExtensionHelpersChildrenProps {
  deleteExtension(extensionId: string): Promise<void>;
}

export interface IWithExtensionHelpersProps {
  children(props: IWithExtensionHelpersChildrenProps): any;
}

export class WithExtensionHelpersWrapped extends React.Component<
  IWithExtensionHelpersProps & IApiContext
> {
  constructor(props: IWithExtensionHelpersProps & IApiContext) {
    super(props);
    this.deleteExtension = this.deleteExtension.bind(this);
  }

  /**
   * Deletes the extension with the specified identifier.
   * @param extensionId the ID of the extension being deleted
   */
  public async deleteExtension(extensionId: string): Promise<void> {
    const response = await callFetch({
      headers: this.props.headers,
      method: 'DELETE',
      url: `${this.props.apiUri}/extensions/${extensionId}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

  public render() {
    return this.props.children({
      deleteExtension: this.deleteExtension,
    });
  }
}

export const WithExtensionHelpers: React.FunctionComponent<
  IWithExtensionHelpersProps
> = props => (
  <ApiContext.Consumer>
    {apiContext => <WithExtensionHelpersWrapped {...props} {...apiContext} />}
  </ApiContext.Consumer>
);
