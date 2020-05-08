import {
  BuildStatus,
  ImportSources,
  ImportSourcesStatus,
  QueryResults,
  RoleInfo,
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

export interface ISaveViewDefinitionResult {
  versionConflict: boolean;
  hasError: boolean;
  message?: string;
  viewDefinition?: ViewDefinition;
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
      usedBy: [] as string[],
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
   * Updates virtualization roles.
   * @param virtName the name of the virtualization
   * @param roleInfo the role info for the virtualization
   */
  const updateVirtualizationRoles = async (
    virtName: string,
    roleInfo: RoleInfo
  ): Promise<void> => {
    const response = await callFetch({
      body: roleInfo,
      headers: {},
      method: 'PUT',
      url: `${apiContext.dvApiUri}virtualizations/${virtName}/roles`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  };

  /**
   * Deletes the virtualization with the specified name.
   * @param virtualizationName the name of the virtualization being deleted
   * @throws an `Error` if there was a problem deleting the virtualization
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
      return Promise.reject(new Error(response.statusText));
    }

    return Promise.resolve();
  };

  /**
   * Requests a `.zip` file of the virtualization be exported to the filesystem.
   * @param name the name of the virtualization
   * @param revision the revision being exported or `undefined` if the current working state should be exported
   * @param fileName the name of the output file (must end with `.zip` or won't be used)
   * @throws an `Error` if there was a problem exporting the file
   */
  const exportVirtualization = async (
    name: string, 
    revision?: number, 
    fileName?: string
  ) => {
    let zipName = fileName;

    if (!zipName || !zipName.endsWith('.zip')) {
      zipName = `${name}`;
      if (revision) {
        zipName += `-v` + revision;
      }
      zipName += '-export.zip';
    }

    const url = `${apiContext.dvApiUri}virtualizations/${name}/export`;

    const response = await callFetch({
      headers: apiContext.headers,
      method: 'GET',
      url: revision ? `${url}/${revision}` : url,
    });

    if (!response.ok) {
      return Promise.reject(new Error(response.statusText));
    }

    // return zip file
    return saveAs(await response.blob(), zipName);
  };

  /**
   * Uploads and imports the supplied file as a new virtualization. If an error does occur, the
   * `Error.name` contains the stringified response status code.
   * @param file the zip file being processed
   * @throws an `Error` if there was a problem uploading or importing the file.
   */
  const importVirtualization = async (file: File) => {
    const data = new FormData();
    data.append('file', file, file.name);

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
      url: `${apiContext.dvApiUri}virtualizations`,
    });

    if (!response.ok) {
      const error = new Error(response.statusText);
      error.name = response.status.toString();
      return Promise.reject(error);
    }

    return Promise.resolve();
  };

  /**
   * Publish the virtualization with the specified name.
   * @param virtualizationName the name of the virtualization being published
   * @returns the `TeiidStatus` model object
   * @throws an `Error` if there was a problem submitting the virtualization for publishing.
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
      return Promise.reject(new Error(response.statusText));
    }

    const status = (await response.json()) as TeiidStatus;
    if (status.attributes.error) {
      return Promise.reject(new Error(status.attributes.error));
    }

    return status;
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
   * Refresh the schema for the specified source.  This triggers the backend to start the refresh.
   * @param connectionName the name of the connection to refresh
   * @throws an `Error` if there was a problem with the refresh submittal
   */
  const refreshConnectionSchema = async (
    connectionName: string,
  ): Promise<void> => {
    const response = await callFetch({
      headers: {},
      method: 'POST',
      url: `${
        apiContext.dvApiUri
      }metadata/refreshSchema/${connectionName}`,
    });
    
    if (!response.ok) {
      const result = await response.json();
      throw new Error(result.message);
    }

    return Promise.resolve();
  };

  /**
   * Revert the virtualization with the specified name to the specified publish edition
   * @param virtualizationName the name of the virtualization being published
   * @param virtualizationRevision the revision to revert the working virtualization
   * @returns the `TeiidStatus` model object
   * @throws an `Error` if there was a problem reverting the virtualization
   */
  const revertVirtualization = async (
    virtualizationName: string,
    virtualizationRevision: number
  ): Promise<TeiidStatus> => {
    const response = await callFetch({
      headers: {},
      method: 'POST',
      url: `${
        apiContext.dvApiUri
      }virtualizations/publish/${virtualizationName}/${virtualizationRevision}/revert`,
    });

    if (!response.ok) {
      return Promise.reject(new Error(response.statusText));
    }

    const status = (await response.json()) as TeiidStatus;
    if (status.attributes.error) {
      return Promise.reject(new Error(status.attributes.error));
    }

    return status;
  };

  /**
   * Start the specified virtualization version.
   * @param virtualizationName the name of the virtualization being started
   * @param virtualizationRevision the version of the virtualization being started
   * @returns the `TeiidStatus` model object
   * @throws an `Error` if there was a problem starting the virtualization.
   */
  const startVirtualization = async (
    virtualizationName: string,
    virtualizationRevision: number
  ): Promise<TeiidStatus> => {
    const response = await callFetch({
      headers: {},
      method: 'POST',
      url: `${
        apiContext.dvApiUri
      }virtualizations/publish/${virtualizationName}/${virtualizationRevision}/start`,
    });

    if (!response.ok) {
      return Promise.reject(new Error(response.statusText));
    }

    const status = (await response.json()) as TeiidStatus;
    if (status.attributes.error) {
      return Promise.reject(new Error(status.attributes.error));
    }

    return status;
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
      url: `${apiContext.dvApiUri}editors/${viewDefinitionId}`,
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
      url: `${apiContext.dvApiUri}virtualizations/${encodedName}`,
    });

    // ok response - the virtualization already exists
    if (response.ok) {
      return {
        hasError: false,
        nameExists: true,
      };
      // 403 response - the supplied name is invalid
      // 303 response - the supplied name matches a source name
    } else if (
      !response.ok &&
      (response.status === 403 || response.status === 303)
    ) {
      const result = await response.json();
      return {
        hasError: true,
        message: result.message,
        nameExists: false,
      };
    }

    // no validation problems
    return {
      hasError: false,
      nameExists: false,
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
        nameExists: true,
      };
      // 403 response - the supplied name is invalid
      // 303 response - the supplied name matches a source name
    } else if (
      !response.ok &&
      (response.status === 403 || response.status === 303)
    ) {
      const result = await response.json();
      return {
        hasError: true,
        message: result.message,
        nameExists: false,
      };
    }

    // no validation problems
    return {
      hasError: false,
      nameExists: false,
    };
  };

  /**
   * Unpublish the virtualization with the specified name
   * @param virtualizationName the name of the virtualization being unpublished
   * @returns the `BuildStatus` model object
   */
  const unpublishVirtualization = async (
    virtualizationName: string
  ): Promise<BuildStatus> => {
    const response = await callFetch({
      headers: {},
      method: 'DELETE',
      url: `${
        apiContext.dvApiUri
      }virtualizations/publish/${virtualizationName}`,
    });

    if (!response.ok) {
      return Promise.reject(new Error(response.statusText));
    }

    const status = (await response.json()) as BuildStatus;
    if (status.status === 'NOTFOUND') {
      const e = new Error('Already unpublished');
      e.name = 'AlreadyUnpublished';
      return Promise.reject(e);
    }

    if (status.status !== 'DELETE_SUBMITTED') {
      return Promise.reject(new Error(status.statusMessage));
    }

    return status;
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
      url: `${apiContext.dvApiUri}editors/${viewDefinitionId}`,
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
  ): Promise<ISaveViewDefinitionResult> => {
    const response = await callFetch({
      body: viewDefinition,
      headers: {},
      method: 'PUT',
      url: `${apiContext.dvApiUri}editors`,
    });

    // Response problem - determine if version conflict
    if (!response.ok) {
      return {
        hasError: true,
        message: response.statusText,
        versionConflict: response.status === 409,
      };
    } else {
      const viewDefn = (await response.json()) as ViewDefinition;
      return {
        hasError: false,
        versionConflict: false,
        viewDefinition: viewDefn,
      }
    }
  };

  /**
   * Get the Source info for a Virtualization
   * @param virtualalization the virtualization
   */
  const getSourceInfoForView = async (
    virtualizationName: string
  ): Promise<ViewSourceInfo> => {
    const response = await callFetch({
      headers: {},
      method: 'GET',
      url: `${apiContext.dvApiUri}metadata/runtimeMetadata/${
        virtualizationName
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
    exportVirtualization,
    getSourceInfoForView,
    getView,
    getViewDefinition,
    importSource,
    importVirtualization,
    publishVirtualization,
    queryVirtualization,
    refreshConnectionSchema,
    revertVirtualization,
    saveViewDefinition,
    startVirtualization,
    unpublishVirtualization,
    updateVirtualizationDescription,
    updateVirtualizationRoles,
    validateViewName,
    validateVirtualizationName,
  };
};
