import { WithViewEditorStates, WithVirtualizationHelpers } from '@syndesis/api';
import { RestDataService, ViewEditorState, ViewInfo } from '@syndesis/models';
import { ViewsCreateLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import resolvers from '../../../resolvers';
import { ViewInfosContent, ViewsCreateSteps } from '../../shared';
import { generateViewEditorStates } from '../../shared/VirtualizationUtils';

/**
 * @param virtualizationId - the ID of the virtualization for the wizard.
 */
export interface ISelectViewsRouteParams {
  virtualizationId: string;
}

/**
 * @param virtualization - the virtualization for the wizard.
 * @param connectionId - the id of the selected connection
 */
export interface ISelectViewsRouteState {
  virtualization: RestDataService;
  connectionId: string;
}

export interface ISelectViewsPageState {
  hasSelectedTables: boolean;
}

export class SelectViewsPage extends React.Component<
  {},
  ISelectViewsPageState
> {
  public selectedViews: ViewInfo[] = []; // Maintains list of selected views

  public constructor(props: {}) {
    super(props);
    this.state = {
      hasSelectedTables: false, // initialize selected tables state
    };
    this.handleAddView = this.handleAddView.bind(this);
    this.handleRemoveView = this.handleRemoveView.bind(this);
  }

  public getExistingViewNames(editorStates: ViewEditorState[]) {
    const viewNames: string[] = [];
    for (const editorState of editorStates) {
      viewNames.push(editorState.viewDefinition.viewName);
    }
    return viewNames;
  }

  public handleAddView(view: ViewInfo) {
    this.selectedViews.push(view);
    this.setState({
      hasSelectedTables: this.selectedViews.length > 0,
    });
  }

  public handleRemoveView(viewName: string) {
    const index = this.selectedViews.findIndex(
      view => view.viewName === viewName
    );

    if (index !== -1) {
      this.selectedViews.splice(index, 1);
    }
    this.setState({
      hasSelectedTables: this.selectedViews.length > 0,
    });
  }

  public render() {
    return (
      <WithRouteData<ISelectViewsRouteParams, ISelectViewsRouteState>>
        {(
          { virtualizationId },
          { virtualization, connectionId },
          { history }
        ) => (
          // TODO need to retrieve real user here
          <WithVirtualizationHelpers username="developer">
            {({ refreshVirtualizationViews }) => {
              const handleCreateViews = async () => {
                const viewEditorStates = generateViewEditorStates(
                  virtualization.serviceVdbName,
                  this.selectedViews
                );
                await refreshVirtualizationViews(
                  virtualization.keng__id,
                  viewEditorStates
                );
                // TODO: post toast notification
                history.push(
                  resolvers.data.virtualizations.views.root({ virtualization })
                );
              };
              return (
                <WithViewEditorStates>
                  {({ data, hasData, error }) => (
                    <ViewsCreateLayout
                      header={<ViewsCreateSteps step={2} />}
                      content={
                        <ViewInfosContent
                          connectionName={connectionId}
                          existingViewNames={this.getExistingViewNames(data)}
                          onViewSelected={this.handleAddView}
                          onViewDeselected={this.handleRemoveView}
                        />
                      }
                      cancelHref={resolvers.data.virtualizations.views.root({
                        virtualization,
                      })}
                      backHref={resolvers.data.virtualizations.views.importSource.selectConnection(
                        { virtualization }
                      )}
                      onCreateViews={handleCreateViews}
                      isNextDisabled={!this.state.hasSelectedTables}
                      isNextLoading={false}
                      isLastStep={true}
                    />
                  )}
                </WithViewEditorStates>
              );
            }}
          </WithVirtualizationHelpers>
        )}
      </WithRouteData>
    );
  }
}
