import {
  QueryResults,
  RestDataService,
  ViewDefinition,
  ViewDefinitionStatus,
  ViewEditorState,
} from '@syndesis/models';
import * as React from 'react';
import { ApiContext, IApiContext } from './ApiContext';
import { callFetch } from './callFetch';

const WORKSPACE_ROOT = '/tko:komodo/tko:workspace/';

export interface IDvNameValidationResult {
  isError: boolean;
  error?: string;
}

export interface IWithVirtualizationHelpersChildrenProps {
  createVirtualization(
    username: string,
    virtualizationName: string,
    virtualizationDescription?: string
  ): Promise<RestDataService>;
  deleteView(virtualization: RestDataService, viewName: string): Promise<void>;
  deleteViewEditorState(viewEditorStateId: string): Promise<void>;
  deleteVirtualization(virtualizationName: string): Promise<void>;
  publishVirtualization(virtualizationName: string): Promise<void>;
  queryVirtualization(
    virtualizationName: string,
    query: string,
    limit: number,
    offset: number
  ): Promise<QueryResults>;
  refreshVirtualizationViews(
    virtualizationName: string,
    viewEditorStates: ViewEditorState[]
  ): Promise<void>;
  unpublishServiceVdb(vdbName: string): Promise<void>;
  updateViewEditorStates(viewEditorStates: ViewEditorState[]): Promise<void>;
  validateViewDefinition(
    viewDefinition: ViewDefinition
  ): Promise<ViewDefinitionStatus>;
  validateVirtualizationName(
    virtualizationName: string
  ): Promise<IDvNameValidationResult>;
  validateViewName(
    vdbName: string,
    modelName: string,
    viewName: string
  ): Promise<IDvNameValidationResult>;
}

export interface IWithVirtualizationHelpersProps {
  children(props: IWithVirtualizationHelpersChildrenProps): any;
}

export class WithVirtualizationHelpersWrapped extends React.Component<
  IWithVirtualizationHelpersProps & IApiContext
> {
  constructor(props: IWithVirtualizationHelpersProps & IApiContext) {
    super(props);
    this.createVirtualization = this.createVirtualization.bind(this);
    this.updateViewEditorStates = this.updateViewEditorStates.bind(this);
    this.deleteView = this.deleteView.bind(this);
    this.deleteViewEditorState = this.deleteViewEditorState.bind(this);
    this.deleteVirtualization = this.deleteVirtualization.bind(this);
    this.publishVirtualization = this.publishVirtualization.bind(this);
    this.queryVirtualization = this.queryVirtualization.bind(this);
    this.refreshVirtualizationViews = this.refreshVirtualizationViews.bind(
      this
    );
    this.unpublishServiceVdb = this.unpublishServiceVdb.bind(this);
    this.validateViewDefinition = this.validateViewDefinition.bind(this);
    this.validateVirtualizationName = this.validateVirtualizationName.bind(
      this
    );
    this.validateViewName = this.validateViewName.bind(this);
  }

  /**
   * Creates a virtualization with the specified name and description
   * @param username the username (used to define the workspace path)
   * @param virtName the name of the virtualization to create
   * @param virtDesc the description (optional) of the virtualization to create
   */
  public async createVirtualization(
    username: string,
    virtName: string,
    virtDesc?: string
  ): Promise<RestDataService> {
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
      url: `${this.props.dvApiUri}workspace/dataservices/${virtName}`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve(newVirtualization);
  }

  /**
   * Deletes the specified virtualization view.
   * @param virtualization the virtualization
   * @param viewName the name of the view being deleted
   */
  public async deleteView(
    virtualization: RestDataService,
    viewName: string
  ): Promise<void> {
    const vdbName = virtualization.serviceVdbName;
    const editorStateId = vdbName + '.' + viewName;
    // Delete viewEditorState
    await this.deleteViewEditorState(editorStateId);
    // Delete virtualization view
    const response = await callFetch({
      headers: {},
      method: 'DELETE',
      url: `${
        this.props.dvApiUri
      }workspace/vdbs/${vdbName}/Models/views/Views/${viewName}`,
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

  /**
   * Publish the virtualization with the specified name.
   * @param virtualizationName the name of the virtualization being published
   */
  public async publishVirtualization(
    virtualizationName: string
  ): Promise<void> {
    const pubVirtualization = {
      name: `${virtualizationName}`,
    };

    const response = await callFetch({
      body: pubVirtualization,
      headers: {},
      method: 'POST',
      url: `${this.props.dvApiUri}metadata/publish`,
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
  public async queryVirtualization(
    virtualizationName: string,
    sqlQuery: string,
    rowlimit: number,
    rowOffset: number
  ): Promise<QueryResults> {
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
      url: `${this.props.dvApiUri}metadata/query`,
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
  public async validateViewDefinition(
    viewDefinition: ViewDefinition
  ): Promise<ViewDefinitionStatus> {
    const response = await callFetch({
      body: viewDefinition,
      headers: {},
      method: 'POST',
      url: `${this.props.dvApiUri}service/userProfile/validateViewDefinition`,
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
  public async validateVirtualizationName(
    virtName: string
  ): Promise<IDvNameValidationResult> {
    const encodedName = encodeURIComponent(virtName);
    const response = await callFetch({
      headers: {},
      method: 'GET',
      url: `${
        this.props.dvApiUri
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
  public async validateViewName(
    vdbName: string,
    modelName: string,
    viewName: string
  ): Promise<IDvNameValidationResult> {
    const encodedName = encodeURIComponent(viewName);
    const response = await callFetch({
      headers: {},
      method: 'GET',
      url: `${
        this.props.dvApiUri
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
  public async unpublishServiceVdb(vdbName: string): Promise<void> {
    const response = await callFetch({
      headers: {},
      method: 'DELETE',
      url: `${this.props.dvApiUri}metadata/publish/${vdbName}`,
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
  public async deleteViewEditorState(viewEditorStateId: string): Promise<void> {
    const response = await callFetch({
      headers: {},
      method: 'DELETE',
      url: `${
        this.props.dvApiUri
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
  public async updateViewEditorStates(
    viewEditorStates: ViewEditorState[]
  ): Promise<void> {
    const response = await callFetch({
      body: viewEditorStates,
      headers: {},
      method: 'PUT',
      url: `${this.props.dvApiUri}service/userProfile/viewEditorStates`,
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
  public async refreshVirtualizationViews(
    virtualizationName: string,
    viewEditorStates: ViewEditorState[]
  ): Promise<void> {
    // Updates the view editor states
    await this.updateViewEditorStates(viewEditorStates);
    const response = await callFetch({
      headers: {},
      method: 'POST',
      url: `${
        this.props.dvApiUri
      }workspace/dataservices/refreshViews/${virtualizationName}`,
    });
    if (!response.ok) {
      throw new Error(response.statusText);
    }

    return Promise.resolve();
  }

  public render() {
    return this.props.children({
      createVirtualization: this.createVirtualization,
      deleteView: this.deleteView,
      deleteViewEditorState: this.deleteViewEditorState,
      deleteVirtualization: this.deleteVirtualization,
      publishVirtualization: this.publishVirtualization,
      queryVirtualization: this.queryVirtualization,
      refreshVirtualizationViews: this.refreshVirtualizationViews,
      unpublishServiceVdb: this.unpublishServiceVdb,
      updateViewEditorStates: this.updateViewEditorStates,
      validateViewDefinition: this.validateViewDefinition,
      validateViewName: this.validateViewName,
      validateVirtualizationName: this.validateVirtualizationName,
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
