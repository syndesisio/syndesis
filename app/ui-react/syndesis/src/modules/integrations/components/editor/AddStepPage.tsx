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
import { IBaseRouteParams, IBaseRouteState } from './interfaces';
import { getStepHref, IGetStepHrefs } from './utils';

export interface IAddStepPageProps extends IGetStepHrefs {
  cancelHref: (p: IBaseRouteParams, s: IBaseRouteState) => H.LocationDescriptor;
  getAddMapperStepHref: (
    position: number,
    p: IBaseRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
  getAddStepHref: (
    position: number,
    p: IBaseRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
  getEditStepHref: (
    position: number,
    p: IBaseRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptorObject;
  saveHref: (p: IBaseRouteParams, s: IBaseRouteState) => H.LocationDescriptor;
  selfHref: (
    p: IBaseRouteParams,
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
      step: step,
    });
  }

  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <>
            <WithRouteData<IBaseRouteParams, IBaseRouteState>>
              {({ flowId }, { integration }, { history }) => {
                const onDelete = (idx: number, step: Step): void => {
                  if (
                    idx === getFirstPosition(integration, flowId) ||
                    idx === getLastPosition(integration, flowId)
                  ) {
                    history.push(
                      this.props.getEditStepHref(
                        this.state.position!,
                        { flowId },
                        { integration }
                      )
                    );
                  }

                  this.setStepAndPosition(idx, step);
                  this.openDeleteDialog();
                };

                return (
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

                          const newInt = removeStepFromFlow(
                            integration,
                            flowId,
                            this.state.position!
                          );

                          history.push(
                            this.props.selfHref(
                              { flowId },
                              { integration: newInt }
                            )
                          );
                        }}
                      />
                    )}
                    <PageTitle title={t('integrations:editor:saveOrAddStep')} />
                    <IntegrationEditorLayout
                      title={t('integrations:editor:addToIntegration')}
                      description={t('integrations:editor:addStepDescription')}
                      content={
                        <IntegrationEditorStepAdder
                          steps={getSteps(integration, flowId)}
                          addDataMapperStepHref={position =>
                            this.props.getAddMapperStepHref(
                              position,
                              { flowId },
                              { integration }
                            )
                          }
                          addStepHref={position =>
                            this.props.getAddStepHref(
                              position,
                              { flowId },
                              { integration }
                            )
                          }
                          configureStepHref={(position: number, step: Step) =>
                            getStepHref(
                              step,
                              { flowId, position: `${position}` },
                              { integration },
                              this.props
                            )
                          }
                          flowId={flowId}
                          integration={integration}
                          onDelete={onDelete}
                        />
                      }
                      cancelHref={this.props.cancelHref(
                        { flowId },
                        { integration }
                      )}
                      saveHref={this.props.saveHref(
                        { flowId },
                        { integration }
                      )}
                      publishHref={this.props.saveHref(
                        { flowId },
                        { integration }
                      )}
                    />
                  </>
                );
              }}
            </WithRouteData>
          </>
        )}
      </Translation>
    );
  }
}
