import {
  getFirstPosition,
  getLastPosition,
  getSteps,
  removeStepFromFlow,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Step } from '@syndesis/models';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
  IntegrationEditorLayout,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../shared';
import { IntegrationEditorStepAdder } from '../IntegrationEditorStepAdder';
import {
  IBaseFlowRouteParams,
  IBaseRouteState,
  IPageWithEditorBreadcrumb,
} from './interfaces';
import { getStepHref, IGetStepHrefs } from './utils';

export interface IAddStepPageProps
  extends IGetStepHrefs,
    IPageWithEditorBreadcrumb {
  cancelHref: (
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
  getAddMapperStepHref: (
    position: number,
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
  getAddStepHref: (
    position: number,
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
  getDeleteEdgeStepHref: (
    position: number,
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptorObject;
  saveHref: (
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
  selfHref: (
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptorObject;
}

export interface IAddStepPageState {
  position?: number;
  showDeleteDialog: boolean;
  step?: Step;
}

/**
 * This page shows the steps of an existing integration.
 *
 * This component expects a [state]{@link IBaseRouteState} to be properly set in
 * the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 *
 * @todo make this page shareable by making the [integration]{@link IBaseRouteState#integration}
 * optional and adding a WithIntegration component to retrieve the integration
 * from the backend
 */
export class AddStepPage extends React.Component<
  IAddStepPageProps,
  IAddStepPageState
> {
  constructor(props: any) {
    super(props);
    this.state = {
      position: 0,
      showDeleteDialog: false,
      step: {},
    };

    this.closeDeleteDialog = this.closeDeleteDialog.bind(this);
    this.openDeleteDialog = this.openDeleteDialog.bind(this);
    this.handleDeleteConfirm = this.handleDeleteConfirm.bind(this);
    this.setStepAndPosition = this.setStepAndPosition.bind(this);
  }

  public closeDeleteDialog(): void {
    this.setState({
      showDeleteDialog: false,
    });
  }

  public openDeleteDialog(): void {
    this.setState({
      showDeleteDialog: true,
    });
  }

  public handleDeleteConfirm() {
    if (this.state.showDeleteDialog) {
      this.closeDeleteDialog();
    }
  }

  public setStepAndPosition(idx: number, step: Step): void {
    this.setState({
      position: idx,
      step,
    });
  }

  public render() {
    const onDelete = (idx: number, step: Step): void => {
      this.setStepAndPosition(idx, step);
      this.openDeleteDialog();
    };

    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <>
            <WithRouteData<IBaseFlowRouteParams, IBaseRouteState>>
              {(params, state, { history }) => (
                <>
                  {this.state.showDeleteDialog && (
                    <ConfirmationDialog
                      buttonStyle={ConfirmationButtonStyle.NORMAL}
                      icon={ConfirmationIconType.DANGER}
                      i18nCancelButtonText={t('shared:Cancel')}
                      i18nConfirmButtonText={t('shared:Delete')}
                      i18nConfirmationMessage={t(
                        'integrations:editor:confirmDeleteStepDialogBody'
                      )}
                      i18nTitle={t(
                        'integrations:editor:confirmDeleteStepDialogTitle'
                      )}
                      showDialog={this.state.showDeleteDialog}
                      onCancel={this.closeDeleteDialog}
                      onConfirm={() => {
                        this.handleDeleteConfirm();

                        /**
                         * Check if step is first or last position,
                         * in which case you should delete the step and
                         * subsequently redirect the user to the step select
                         * page for that position.
                         */
                        if (
                          this.state.position ===
                            getFirstPosition(
                              state.integration,
                              params.flowId
                            ) ||
                          this.state.position ===
                            getLastPosition(state.integration, params.flowId)
                        ) {
                          history.push(
                            this.props.getDeleteEdgeStepHref(
                              this.state.position!,
                              params,
                              state
                            )
                          );
                        } else {
                          /**
                           * Remove the step from the integration flow
                           * and receive a copy of the new integration.
                           */
                          const newInt = removeStepFromFlow(
                            state.integration,
                            params.flowId,
                            this.state.position!
                          );

                          /**
                           * If is a middle step, simply remove the step
                           * and update the UI.
                           */
                          history.push(
                            this.props.selfHref(params, {
                              ...state,
                              integration: newInt,
                            })
                          );
                        }
                      }}
                    />
                  )}
                  <PageTitle title={t('integrations:editor:saveOrAddStep')} />
                  <IntegrationEditorLayout
                    title={t('integrations:editor:addToIntegration')}
                    description={t('integrations:editor:addStepDescription')}
                    toolbar={this.props.getBreadcrumb(
                      t('integrations:editor:addToIntegration'),
                      params,
                      state
                    )}
                    content={
                      <IntegrationEditorStepAdder
                        steps={getSteps(state.integration, params.flowId)}
                        addDataMapperStepHref={position =>
                          this.props.getAddMapperStepHref(
                            position,
                            params,
                            state
                          )
                        }
                        addStepHref={position =>
                          this.props.getAddStepHref(position, params, state)
                        }
                        configureStepHref={(position: number, step: Step) =>
                          getStepHref(
                            step,
                            { ...params, position: `${position}` },
                            state,
                            this.props
                          )
                        }
                        flowId={params.flowId}
                        integration={state.integration}
                        onDelete={onDelete}
                      />
                    }
                    cancelHref={this.props.cancelHref(params, state)}
                    saveHref={this.props.saveHref(params, state)}
                    publishHref={this.props.saveHref(params, state)}
                  />
                </>
              )}
            </WithRouteData>
          </>
        )}
      </Translation>
    );
  }
}
