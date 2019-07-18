import {
  QueryResults,
  RestDataService,
  ViewDefinition,
  ViewDefinitionStatus,
  ViewEditorState,
} from '@syndesis/models';
import * as React from 'react';
import { ApiContext } from './ApiContext';
import { callFetch } from './callFetch';

export interface IDvNameValidationResult {
  isError: boolean;
  error?: string;
}

export const useVirtualizationHelpers = () => {
  const apiContext = React.useContext(ApiContext);
  const WORKSPACE_ROOT = '/tko:komodo/tko:workspace/';

  /**
   * Creates a virtualization with the specified name and description
   * @param username the username (used to define the workspace path)
   * @param virtName the name of the virtualization to create
   * @param virtDesc the description (optional) of the virtualization to create
   */
  const createVirtualization = async (
    username: string,
    virtName: string,
    virtDesc?: string
  ): Promise<RestDataService> => {
    const newVirtualization = {
      keng__dataPath: `${WORKSPACE_ROOT}${username}/${virtName}`,
      keng__id: `${virtName}`,
      serviceVdbName: `${virtName}`.toLowerCase() + 'vdb',
      tko__description: virtDesc ? `${virtDesc}` : '',
    } as RestDataService;

    const response = await callFetch({
      body: newVirtualization,
      headers: {},
      method: 'POST',
      url: `${apiContext.dvApiUri}workspace/dataservices/${virtName}`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve(newVirtualization);
  }

  /**
   * Updates a virtualization.  Currently this will just update the description
   * @param username the username (used to define the workspace path)
   * @param virtName the name of the virtualization
   * @param virtDesc the description of the virtualization
   */
  const updateVirtualizationDescription = async (
    username: string,
    virtName: string,
    virtDesc: string
  ): Promise<void> => {
    
    const updatedVirtualization = {
      keng__dataPath: `${WORKSPACE_ROOT}${username}/${virtName}`,
      keng__id: `${virtName}`,
      serviceVdbName: `${virtName}`.toLowerCase() + 'vdb',
      tko__description: virtDesc,
    } as RestDataService;

    const response = await callFetch({
      body: updatedVirtualization,
      headers: {},
      method: 'PUT',
      url: `${apiContext.dvApiUri}workspace/dataservices/${virtName}`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

  /**
   * Deletes the specified virtualization view.
   * @param virtualization the virtualization
   * @param viewName the name of the view being deleted
   */
  const deleteView = async (
    virtualization: RestDataService,
    viewName: string
  ): Promise<void> => {
    const vdbName = virtualization.serviceVdbName;
    const editorStateId = vdbName + '.' + viewName;
    // Delete viewEditorState and refresh views
    await deleteViewEditorState(editorStateId);
    const response = await callFetch({
      headers: {},
      method: 'POST',
      url: `${apiContext.dvApiUri}workspace/dataservices/refreshViews/${
        virtualization.keng__id
      }`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

  /**
   * Deletes the virtualization with the specified name.
   * @param virtualizationName the name of the virtualization being deleted
   */
  const deleteVirtualization =  async (virtualizationName: string): Promise<void> => {
    const response = await callFetch({
      headers: {},
      method: 'DELETE',
      url: `${apiContext.dvApiUri}workspace/dataservices/${virtualizationName}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

  /**
   * Publish the virtualization with the specified name.
   * @param virtualizationName the name of the virtualization being published
   */
  const publishVirtualization = async (
    virtualizationName: string
  ): Promise<void> => {
    const pubVirtualization = {
      name: `${virtualizationName}`,
    };

    const response = await callFetch({
      body: pubVirtualization,
      headers: {},
      method: 'POST',
      url: `${apiContext.dvApiUri}metadata/publish`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

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
  }

  /**
   * Validate the supplied ViewDefinition
   * @param viewDefinition the view definition
   */
  const validateViewDefinition = async (
    viewDefinition: ViewDefinition
  ): Promise<ViewDefinitionStatus> => {
    const response = await callFetch({
      body: viewDefinition,
      headers: {},
      method: 'POST',
      url: `${apiContext.dvApiUri}service/userProfile/validateViewDefinition`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return (await response.json()) as ViewDefinitionStatus;
  }

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
      }workspace/dataservices/nameValidation/${encodedName}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    // return validation result
    const result = await response.text();
    const hasError = result.length > 0;
    return {
      error: hasError ? result : '',
      isError: hasError,
    } as IDvNameValidationResult;
  }

  /**
   * Validate the view name for the specified vdb and model
   * @param vdbName the VDB name
   * @param modelName the model name
   * @param viewName the view name
   */
  const validateViewName = async (
    vdbName: string,
    modelName: string,
    viewName: string
  ): Promise<IDvNameValidationResult> => {
    const encodedName = encodeURIComponent(viewName);
    const response = await callFetch({
      headers: {},
      method: 'GET',
      url: `${
        apiContext.dvApiUri
      }workspace/vdbs/${vdbName}/Models/${modelName}/Views/nameValidation/${encodedName}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    // return validation result
    const result = await response.text();
    const hasError = result.length > 0;
    return {
      error: hasError ? result : '',
      isError: hasError,
    } as IDvNameValidationResult;
  }

  /**
   * Unpublish the Service VDB with the specified name.
   * @param vdbName the name of the vdb associated with the service
   */
  const unpublishServiceVdb = async (vdbName: string): Promise<void> => {
    const response = await callFetch({
      headers: {},
      method: 'DELETE',
      url: `${apiContext.dvApiUri}metadata/publish/${vdbName}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

  /**
   * Delete the specified ViewEditorState in the komodo user profile
   * @param viewEditorState the view editor state
   */
  const deleteViewEditorState = async (viewEditorStateId: string): Promise<void> => {
    const response = await callFetch({
      headers: {},
      method: 'DELETE',
      url: `${
        apiContext.dvApiUri
      }service/userProfile/viewEditorState/${viewEditorStateId}`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

  /**
   * Saves ViewEditorStates in the komodo user profile
   * @param viewEditorStates the array of view editor states
   */
  const updateViewEditorStates = async (
    viewEditorStates: ViewEditorState[]
  ): Promise<void> => {
    const response = await callFetch({
      body: viewEditorStates,
      headers: {},
      method: 'PUT',
      url: `${apiContext.dvApiUri}service/userProfile/viewEditorStates`,
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

  /**
   * Saves ViewEditorStates in the komodo user profile, then updates the virtualizations
   * @param viewEditorStates the array of view editor states
   */
  const refreshVirtualizationViews = async (
    virtualizationName: string,
    viewEditorStates: ViewEditorState[]
  ): Promise<void> => {
    // Updates the view editor states
    await updateViewEditorStates(viewEditorStates);
    const response = await callFetch({
      headers: {},
      method: 'POST',
      url: `${
        apiContext.dvApiUri
      }workspace/dataservices/refreshViews/${virtualizationName}`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

  return {
    createVirtualization,
    deleteView,
    deleteVirtualization,
    publishVirtualization,
    queryVirtualization,
    refreshVirtualizationViews,
    unpublishServiceVdb,
    updateVirtualizationDescription,
    validateViewDefinition,
    validateViewName,
    validateVirtualizationName,
  };

};
