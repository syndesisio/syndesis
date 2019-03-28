import { RestDataService, ViewEditorState } from '@syndesis/models';
import * as React from 'react';
import { ApiContext, IApiContext } from './ApiContext';
import { callFetch } from './callFetch';

const WORKSPACE_ROOT = '/tko:komodo/tko:workspace/';

export interface IWithVirtualizationHelpersChildrenProps {
  createVirtualization(
    virtualizationName: string,
    virtualizationDescription?: string
  ): Promise<void>;
  deleteVirtualization(virtualizationName: string): Promise<void>;
  publishVirtualization(virtualizationName: string): Promise<void>;
  refreshVirtualizationViews(
    virtualizationName: string,
    viewEditorStates: ViewEditorState[]
  ): Promise<void>;
  unpublishServiceVdb(vdbName: string): Promise<void>;
  updateViewEditorStates(viewEditorStates: ViewEditorState[]): Promise<void>;
}

export interface IWithVirtualizationHelpersProps {
  username: string;
  children(props: IWithVirtualizationHelpersChildrenProps): any;
}

export class WithVirtualizationHelpersWrapped extends React.Component<
  IWithVirtualizationHelpersProps & IApiContext
> {
  constructor(props: IWithVirtualizationHelpersProps & IApiContext) {
    super(props);
    this.createVirtualization = this.createVirtualization.bind(this);
    this.updateViewEditorStates = this.updateViewEditorStates.bind(this);
    this.deleteVirtualization = this.deleteVirtualization.bind(this);
    this.publishVirtualization = this.publishVirtualization.bind(this);
    this.refreshVirtualizationViews = this.refreshVirtualizationViews.bind(
      this
    );
    this.unpublishServiceVdb = this.unpublishServiceVdb.bind(this);
  }

  /**
   * Creates a virtualization with the specified name and description
   * @param virtName the name of the virtualization to create
   * @param virtDesc the description (optional) of the virtualization to create
   */
  public async createVirtualization(
    virtName: string,
    virtDesc?: string
  ): Promise<void> {
    const newVirtualization = {
      keng__dataPath: `${WORKSPACE_ROOT}${this.props.username}/${virtName}`,
      keng__id: `${virtName}`,
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
      deleteVirtualization: this.deleteVirtualization,
      publishVirtualization: this.publishVirtualization,
      refreshVirtualizationViews: this.refreshVirtualizationViews,
      unpublishServiceVdb: this.unpublishServiceVdb,
      updateViewEditorStates: this.updateViewEditorStates,
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
