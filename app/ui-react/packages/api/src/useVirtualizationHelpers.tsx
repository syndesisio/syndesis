import {
  BuildStatus,
  ImportSources,
  ImportSourcesStatus,
  QueryResults,
  TeiidStatus,
  ViewDefinition,
  ViewSourceInfo,
  Virtualization,
} from '@syndesis/models';
import * as React from 'react';
import { ApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export interface IDvNameValidationResult {
  nameExists: boolean;
  hasError: boolean;
  message?: string;
}
  
export const useVirtualizationHelpers = () => {
  const apiContext = React.useContext(ApiContext);

  /**
   * Creates a virtualization with the specified name and description
   * @param virtName the name of the virtualization to create
   * @param virtDesc the description (optional) of the virtualization to create
   */
  const createVirtualization = async (
    virtName: string,
    virtDesc?: string
  ): Promise<Virtualization> => {
    const newVirtualization = {
      description: virtDesc ? `${virtDesc}` : '',
      name: `${virtName}`,
      usedBy: [] as string[]
    } as Virtualization;

    const response = await callFetch({
      body: newVirtualization,
      headers: {},
      method: 'POST',
      url: `${apiContext.dvApiUri}virtualizations`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve(newVirtualization);
  };

  /**
   * Updates a virtualization.  Currently this will just update the description
   * @param virtName the name of the virtualization
   * @param virtDesc the description of the virtualization
   */
  const updateVirtualizationDescription = async (
    virtName: string,
    virtDesc: string
  ): Promise<void> => {
    const updatedVirtualization = {
      description: virtDesc,
      name: `${virtName}`,
    } as Virtualization;

    const response = await callFetch({
      body: updatedVirtualization,
      headers: {},
      method: 'PUT',
      url: `${apiContext.dvApiUri}virtualizations/${virtName}`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  };

  /**
   * Deletes the virtualization with the specified name.
   * @param virtualizationName the name of the virtualization being deleted
   */
  const deleteVirtualization = async (
    virtualizationName: string
  ): Promise<void> => {
    const response = await callFetch({
      headers: {},
      method: 'DELETE',
      url: `${apiContext.dvApiUri}virtualizations/${virtualizationName}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  };

  /**
   * Publish the virtualization with the specified name.
   * @param virtualizationName the name of the virtualization being published
   * @returns the `TeiidStatus` model object
   */
  const publishVirtualization = async (
    virtualizationName: string
  ): Promise<TeiidStatus> => {
    const pubVirtualization = {
      name: `${virtualizationName}`,
    };

    const response = await callFetch({
      body: pubVirtualization,
      headers: {},
      method: 'POST',
      url: `${apiContext.dvApiUri}virtualizations/publish`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return (await response.json()) as TeiidStatus;
  };

  /**
   * Query the Service VDB with sql query and properties.
   * @param virtualizationName the name of the virtualization associated with the service
   * @param sqlQuery the sql query statement to execute against the virtualization
   * @param rowlimit limit to number of rows to return
   * @param rowOffset number of data rows to filter from the beginning of the result set
   */
  const queryVirtualization = async (
    virtualizationName: string,
    sqlQuery: string,
    rowlimit: number,
    rowOffset: number
  ): Promise<QueryResults> => {
    // The payload for the rest call
    const queryBody = {
      limit: rowlimit,
      offset: rowOffset,
      query: sqlQuery,
      target: virtualizationName,
    };

    const response = await callFetch({
      body: queryBody,
      headers: {},
      method: 'POST',
      url: `${apiContext.dvApiUri}metadata/query`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return (await response.json()) as QueryResults;
  };

  /**
   * Get ViewDefinition for the supplied virtualization and view name
   * @param virtualizationName the name of the virtualization
   * @param viewName the name of the view
   */
  const getView = async (
    virtualizationName: string,
    viewName: string
  ): Promise<ViewDefinition> => {
    const encodedName = encodeURIComponent(viewName);
    const response = await callFetch({
      headers: {},
      method: 'GET',
      url: `${
        apiContext.dvApiUri
      }virtualizations/${virtualizationName}/views/${encodedName}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return (await response.json()) as ViewDefinition;
  };

  /**
   * Get ViewDefinition for the supplied id
   * @param viewDefinitionId the id of the view definition
   */
  const getViewDefinition = async (
    viewDefinitionId: string
  ): Promise<ViewDefinition> => {
    const response = await callFetch({
      headers: {},
      method: 'GET',
      url: `${
        apiContext.dvApiUri
      }editors/${viewDefinitionId}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return (await response.json()) as ViewDefinition;
  };

  /**
   * Import tables from the specified connection source
   * @param virtualizationName the name of the virtualization
   * @param sourceName the name of the source
   * @param importSources the sources for import
   */
  const importSource = async (
    virtualizationName: string,
    sourceName: string,
    importSources: ImportSources
  ): Promise<ImportSourcesStatus> => {
    const response = await callFetch({
      body: importSources,
      headers: {},
      method: 'PUT',
      url: `${
        apiContext.dvApiUri
      }virtualizations/${virtualizationName}/import/${sourceName}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return (await response.json()) as ImportSourcesStatus;
  };

  /**
   * Validate the supplied Virtualization name
   * @param virtName the virutalization name
   */
  const validateVirtualizationName = async (
    virtName: string
  ): Promise<IDvNameValidationResult> => {
    const encodedName = encodeURIComponent(virtName);
    const response = await callFetch({
      headers: {},
      method: 'GET',
      url: `${
        apiContext.dvApiUri
      }virtualizations/${encodedName}`,
    });
    
    // ok response - the virtualization already exists
    if (response.ok) {
      return {
        hasError: false,
        nameExists: true
      }
    // 403 response - the supplied name is invalid
    // 303 response - the supplied name matches a source name
    } else if ( !response.ok && (response.status === 403 || response.status === 303) ) {
      const result = await response.json();
      return {
        hasError: true,
        message: result.message,
        nameExists: false
      }
    } 

    // no validation problems
    return {
      hasError: false,
      nameExists: false
    };
  };

  /**
   * Validate the view name for the specified virtualization
   * @param virtualizationName the virtualization name
   * @param viewName the view name
   */
  const validateViewName = async (
    virtualizationName: string,
    viewName: string
  ): Promise<IDvNameValidationResult> => {
    const encodedName = encodeURIComponent(viewName);
    const response = await callFetch({
      headers: {},
      method: 'GET',
      url: `${
        apiContext.dvApiUri
      }virtualizations/${virtualizationName}/views/${encodedName}`,
    });

    // ok response - the virtualization view already exists
    if (response.ok) {
      return {
        hasError: false,
        nameExists: true
      }
    // 403 response - the supplied name is invalid
    // 303 response - the supplied name matches a source name
    } else if ( !response.ok && (response.status === 403 || response.status === 303) ) {
      const result = await response.json();
      return {
        hasError: true,
        message: result.message,
        nameExists: false
      }
    } 

    // no validation problems
    return {
      hasError: false,
      nameExists: false
    };
  };

  /**
   * Unpublish the virtualization with the specified name
   * @param virtualizationName the name of the virtualization
   * @returns the `BuildStatus` model object
   */
  const unpublishVirtualization = async (
    virtualizationName: string
  ): Promise<BuildStatus> => {
    const response = await callFetch({
      headers: {},
      method: 'DELETE',
      url: `${apiContext.dvApiUri}virtualizations/publish/${virtualizationName}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return (await response.json()) as BuildStatus;
  };

  /**
   * Delete the specified ViewDefinition
   * @param viewDefinitionId the view definition
   */
  const deleteViewDefinition = async (
    viewDefinitionId: string
  ): Promise<void> => {
    const response = await callFetch({
      headers: {},
      method: 'DELETE',
      url: `${
        apiContext.dvApiUri
      }editors/${viewDefinitionId}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  };

  /**
   * Saves the ViewDefinition
   * @param viewDefinition the view definition
   */
  const saveViewDefinition = async (
    viewDefinition: ViewDefinition
  ): Promise<ViewDefinition> => {
    const response = await callFetch({
      body: viewDefinition,
      headers: {},
      method: 'PUT',
      url: `${apiContext.dvApiUri}editors`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }
    return (await response.json()) as ViewDefinition;
  };

  /**
   * Get the Source info for a Virtualization
   * @param virtualalization the virtualization
   */
  const getSourceInfoForView = async (
    virtualization: Virtualization
  ): Promise<ViewSourceInfo> => {
    const response = await callFetch({
      headers: {},
      method: 'GET',
      url: `${apiContext.dvApiUri}metadata/runtimeMetadata/${
        virtualization.name
      }`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }
    return (await response.json()) as ViewSourceInfo;
  };

  return {
    createVirtualization,
    deleteViewDefinition,
    deleteVirtualization,
    getSourceInfoForView,
    getView,
    getViewDefinition,
    importSource,
    publishVirtualization,
    queryVirtualization,
    saveViewDefinition,
    unpublishVirtualization,
    updateVirtualizationDescription,
    validateViewName,
    validateVirtualizationName,
  };
};
