import { Extension } from '@syndesis/models';
import * as React from 'react';
import { ApiContext, IApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export interface IWithExtensionHelpersChildrenProps {
  deleteExtension(extensionId: string): Promise<void>;
  importExtension(extensionId: string): Promise<void>;
  uploadExtension(file: File, extensionId?: string): Promise<Extension>;
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
    this.importExtension = this.importExtension.bind(this);
    this.uploadExtension = this.uploadExtension.bind(this);
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

  /**
   * Imports the extension with the specified identifier.
   * @param extensionId the ID of the extension being imported
   */
  public async importExtension(extensionId: string): Promise<void> {
    const response = await callFetch({
      headers: this.props.headers,
      method: 'POST',
      url: `${this.props.apiUri}/extensions/${extensionId}/install`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

  /**
   * Updates the extension with the specified identifier or creates a new extension if there is no identifier.
   * @param extensionId the ID of the extension being uploaded
   */
  public async uploadExtension(
    file: File,
    extensionId?: string
  ): Promise<Extension> {
    const data = new FormData();
    data.append('file', file, file.name);
    const url = `${this.props.apiUri}/extensions`;
    const {
      Accept,
      ['Content-Type']: contentType,
      ...rest
    } = this.props.headers;
    const response = await callFetch({
      body: data,
      headers: { ...rest },
      includeAccept: false,
      includeContentType: false,
      includeReferrerPolicy: false,
      method: 'POST',
      url: extensionId ? `${url}?updatedId=${extensionId}` : url,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }
    return (await response.json()) as Extension;
  }

  public render() {
    return this.props.children({
      deleteExtension: this.deleteExtension,
      importExtension: this.importExtension,
      uploadExtension: this.uploadExtension,
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
