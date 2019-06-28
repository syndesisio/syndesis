import { Extension } from '@syndesis/models';
import * as React from 'react';
import { ApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export const useExtensionHelpers = () => {
  const apiContext = React.useContext(ApiContext);

  /**
   * Deletes the extension with the specified identifier.
   * @param extensionId the ID of the extension being deleted
   */
  const deleteExtension = async (extensionId: string) => {
    const response = await callFetch({
      headers: apiContext.headers,
      method: 'DELETE',
      url: `${apiContext.apiUri}/extensions/${extensionId}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }
  };

  /**
   * Imports the extension with the specified identifier.
   * @param extensionId the ID of the extension being imported
   */
  const importExtension = async (extensionId: string) => {
    const response = await callFetch({
      headers: apiContext.headers,
      method: 'POST',
      url: `${apiContext.apiUri}/extensions/${extensionId}/install`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }
  };

  /**
   * Updates the extension with the specified identifier or creates a new extension if there is no identifier.
   * @param extensionId the ID of the extension being uploaded
   */
  const uploadExtension = async (file: File, extensionId?: string) => {
    const data = new FormData();
    data.append('file', file, file.name);
    const url = `${apiContext.apiUri}/extensions`;
    const {
      Accept,
      ['Content-Type']: contentType,
      ...rest
    } = apiContext.headers;
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
  };

  return {
    deleteExtension,
    importExtension,
    uploadExtension,
  };
};
