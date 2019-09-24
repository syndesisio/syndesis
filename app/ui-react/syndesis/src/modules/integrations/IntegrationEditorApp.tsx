import {
  ALL_STEPS,
  createStep,
  DATA_MAPPER,
  getChoiceConfigMode,
  getStep
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { StepKind } from '@syndesis/models';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Route, Switch } from 'react-router';
import { WithClosedNavigation } from '../../shared';
import { WithLeaveConfirmation } from '../../shared/WithLeaveConfirmation';
import { AddStepPage } from './components/editor/AddStepPage';
import { EditorApp } from './components/editor/EditorApp';
import { EditorBreadcrumb } from './components/editor/EditorBreadcrumb';
import {
  DataShapeDirection,
  IBaseFlowRouteParams,
  IBaseRouteParams,
  IBaseRouteState,
} from './components/editor/interfaces';
import { OperationsPage } from './components/editor/OperationsPage';
import { SaveIntegrationPage } from './components/editor/SaveIntegrationPage';
import resolvers from './resolvers';
import routes from './routes';

/**
 * Entry point for the integration editor app. This is shown when an user clicks
 * on the "Edit" button for any existing integration.
 *
 * Since all the creation routes will show the same breadcrumb and require the
 * left navigation bar to be closed to reclaim space, we do it here.
 *
 * Almost all of the routes *require* some state to be passed for them to
 * properly work, so an url that works for an user *will not work* for another.
 * If you try and open the same url on a different browser, the code will throw
 * an exception because of this.
 *
 * We should set up an error boundary[1] to catch these errors and tell the user
 * that he reached an invalid url, or redirect him to a safe page.
 *
 * [1] https://reactjs.org/docs/error-boundaries.html
 *
 * @todo add an error handler!
 */
export const IntegrationEditorApp: React.FunctionComponent = () => {
  const { params, state } = useRouteData<
    IBaseFlowRouteParams,
    IBaseRouteState
  >();

  const getBreadcrumb = (
    title: string,
    p: IBaseRouteParams | IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => (
    <EditorBreadcrumb
      integration={state.integration}
      rootHref={resolvers.integration.edit.entryPoint({ ...p, ...s })}
      apiProviderEditorHref={resolvers.integration.edit.editStep.apiProvider.editSpecification(
        {
          ...(p as IBaseFlowRouteParams),
          ...s,
          position: '0',
          specification: s.integration,
        }
      )}
      getFlowHref={flowId =>
        resolvers.integration.edit.index({ ...p, ...s, flowId })
      }
      currentFlowId={(p as IBaseFlowRouteParams).flowId}
    >
      {title}
    </EditorBreadcrumb>
  );

  return (
    <Translation ns={['integrations', 'shared']}>
      {t => {
        const i18nTitle = t('unsavedChangesTitle');
        const i18nConfirmationMessage = t('unsavedChangesMessage');
        const shouldDisplayDialog = (location: H.LocationDescriptor) => {
          const url =
            typeof location === 'string' ? location : location.pathname!;
          return !url.startsWith(
            resolvers.integration.edit.root({
              flowId: params.flowId,
              integration: state.integration,
            }).pathname
          );
        };
        return (
          <WithClosedNavigation>
            <Switch>
              <Route path={routes.integration.edit.index} exact={true}>
                <WithLeaveConfirmation
                  i18nTitle={i18nTitle}
                  i18nConfirmationMessage={i18nConfirmationMessage}
                  shouldDisplayDialog={shouldDisplayDialog}
                >
                  {() => (
                    <AddStepPage
                      cancelHref={resolvers.list}
                      getAddStepHref={(position, p, s) =>
                        resolvers.integration.edit.addStep.selectStep({
                          position: `${position}`,
                          ...p,
                          ...s,
                        })
                      }
                      apiProviderHref={(step, p, s) =>
                        resolvers.integration.edit.editStep.apiProvider.selectMethod(
                          {
                            ...p,
                            ...s,
                          }
                        )
                      }
                      getDeleteEdgeStepHref={(position, p, s) =>
                        resolvers.integration.edit.editStep.selectStep({
                          position: `${position}`,
                          ...p,
                          ...s,
                        })
                      }
                      connectionHref={(step, p, s) =>
                        resolvers.integration.edit.editStep.connection.configureAction(
                          {
                            actionId: step.action!.id!,
                            connection: step.connection!,
                            ...p,
                            ...s,
                          }
                        )
                      }
                      filterHref={(step, p, s) =>
                        resolvers.integration.edit.editStep.basicFilter({
                          step,
                          ...p,
                          ...s,
                        })
                      }
                      choiceHref={(step, p, s) => {
                        const configMode = getChoiceConfigMode(step);
                        if (typeof configMode !== 'undefined') {
                          return resolvers.integration.edit.editStep.choice.configure({
                            configMode,
                            step,
                            ...p,
                            ...s,
                          })
                        } else {
                          return resolvers.integration.edit.editStep.choice.selectMode({
                            step,
                            ...p,
                            ...s,
                          })
                        }
                      }}
                      getAddMapperStepHref={(position, p, s) =>
                        resolvers.integration.edit.addStep.dataMapper({
                          position: `${position}`,
                          step: {
                            ...createStep(),
                            ...ALL_STEPS.find(
                              step => step.stepKind === DATA_MAPPER
                            ),
                          } as StepKind,
                          ...p,
                          ...s,
                        })
                      }
                      mapperHref={(step, p, s) =>
                        resolvers.integration.edit.editStep.dataMapper({
                          step,
                          ...p,
                          ...s,
                        })
                      }
                      templateHref={(step, p, s) =>
                        resolvers.integration.edit.editStep.template({
                          step,
                          ...p,
                          ...s,
                        })
                      }
                      stepHref={(step, p, s) =>
                        resolvers.integration.edit.editStep.step({
                          step,
                          ...p,
                          ...s,
                        })
                      }
                      saveHref={(p, s) =>
                        resolvers.integration.edit.saveAndPublish({
                          ...p,
                          ...s,
                        })
                      }
                      selfHref={(p, s) =>
                        resolvers.integration.edit.index({
                          ...p,
                          ...s,
                        })
                      }
                      getBreadcrumb={getBreadcrumb}
                      getFlowHref={(flowId, p, s) =>
                        resolvers.integration.edit.index({ ...p, ...s, flowId })
                      }
                      getGotoDescribeDataHref={(position, flowId, p, s) => {
                        const step = getStep(
                          state.integration,
                          flowId,
                          position
                        ) as StepKind;
                        return resolvers.integration.edit.editStep.connection.describeData(
                          {
                            ...p,
                            ...{
                              ...s,
                              connection: step.connection!,
                              step,
                            },
                            direction: DataShapeDirection.OUTPUT,
                            position: `${position}`,
                          }
                        );
                      }}
                    />
                  )}
                </WithLeaveConfirmation>
              </Route>

              <Route path={routes.integration.edit.operations} exact={true}>
                <WithLeaveConfirmation
                  i18nTitle={i18nTitle}
                  i18nConfirmationMessage={i18nConfirmationMessage}
                  shouldDisplayDialog={shouldDisplayDialog}
                >
                  {() => (
                    <OperationsPage
                      cancelHref={(p, s) => resolvers.list()}
                      saveHref={(p, s) =>
                        resolvers.integration.edit.saveAndPublish({
                          ...p,
                          ...s,
                        })
                      }
                      getFlowHref={(p, s) =>
                        resolvers.integration.edit.index({ ...p, ...s })
                      }
                      getBreadcrumb={getBreadcrumb}
                    />
                  )}
                </WithLeaveConfirmation>
              </Route>

              {/* add step */}
              <Route path={routes.integration.edit.addStep.selectStep}>
                <WithLeaveConfirmation
                  i18nTitle={i18nTitle}
                  i18nConfirmationMessage={i18nConfirmationMessage}
                  shouldDisplayDialog={shouldDisplayDialog}
                >
                  {() => (
                    <EditorApp
                      mode={'adding'}
                      appStepRoutes={routes.integration.edit.addStep}
                      appResolvers={resolvers.integration.edit.addStep}
                      cancelHref={(p, s) =>
                        resolvers.integration.edit.index({
                          ...p,
                          ...s,
                        })
                      }
                      postConfigureHref={(updatedIntegration, p) =>
                        resolvers.integration.edit.index({
                          ...p,
                          integration: updatedIntegration,
                        })
                      }
                      getBreadcrumb={getBreadcrumb}
                    />
                  )}
                </WithLeaveConfirmation>
              </Route>

              {/* edit step */}
              <Route path={routes.integration.edit.editStep.selectStep}>
                <WithLeaveConfirmation
                  i18nTitle={i18nTitle}
                  i18nConfirmationMessage={i18nConfirmationMessage}
                  shouldDisplayDialog={shouldDisplayDialog}
                >
                  {() => (
                    <EditorApp
                      mode={'editing'}
                      appStepRoutes={routes.integration.edit.editStep}
                      appResolvers={resolvers.integration.edit.editStep}
                      cancelHref={(p, s) =>
                        resolvers.integration.edit.index({
                          ...p,
                          ...s,
                        })
                      }
                      postConfigureHref={(updatedIntegration, p) =>
                        resolvers.integration.edit.index({
                          ...p,
                          integration: updatedIntegration,
                        })
                      }
                      getBreadcrumb={getBreadcrumb}
                    />
                  )}
                </WithLeaveConfirmation>
              </Route>

              <Route path={routes.integration.edit.saveAndPublish} exact={true}>
                <SaveIntegrationPage
                  cancelHref={(p, s) =>
                    resolvers.integration.edit.entryPoint({ ...p, ...s })
                  }
                  postSaveHref={(p, s) =>
                    resolvers.integration.edit.entryPoint({
                      ...p,
                      ...s,
                    })
                  }
                  postPublishHref={resolvers.integration.details}
                  i18nTitle={i18nTitle}
                  i18nConfirmationMessage={i18nConfirmationMessage}
                  shouldDisplayDialog={shouldDisplayDialog}
                  getBreadcrumb={getBreadcrumb}
                />
              </Route>
            </Switch>
          </WithClosedNavigation>
        );
      }}
    </Translation>
  );
};
