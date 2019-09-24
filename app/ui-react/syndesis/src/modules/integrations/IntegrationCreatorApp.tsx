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
 * Entry point for the integration creator app. This is shown when an user clicks
 * the "Create integration" button somewhere in the app.
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
 * @todo i18n everywhere!
 */
export const IntegrationCreatorApp: React.FunctionComponent = () => {
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
      rootHref={resolvers.create.configure.entryPoint({ ...p, ...s })}
      apiProviderEditorHref={resolvers.create.configure.editStep.apiProvider.editSpecification(
        {
          ...(p as IBaseFlowRouteParams),
          ...s,
          position: '0',
          specification: s.integration,
        }
      )}
      getFlowHref={flowId =>
        resolvers.create.configure.index({ ...p, ...s, flowId })
      }
      currentFlowId={(p as IBaseFlowRouteParams).flowId}
    >
      {title}
    </EditorBreadcrumb>
  );

  return (
    <WithClosedNavigation>
      <Translation ns={['integrations']}>
        {t => {
          const i18nTitle = t('unsavedChangesTitle');
          const i18nConfirmationMessage = t('unsavedChangesMessage');
          const shouldDisplayDialog = (location: H.LocationDescriptor) => {
            const url =
              typeof location === 'string' ? location : location.pathname!;
            return !url.startsWith(routes.create.root);
          };

          return (
            <Switch>
              {/* start step */}
              <Route path={routes.create.start.selectStep}>
                <WithLeaveConfirmation
                  i18nTitle={i18nTitle}
                  i18nConfirmationMessage={i18nConfirmationMessage}
                  shouldDisplayDialog={shouldDisplayDialog}
                >
                  {() => (
                    <EditorApp
                      mode={'adding'}
                      appStepRoutes={routes.create.start}
                      appResolvers={resolvers.create.start}
                      cancelHref={resolvers.list}
                      postConfigureHref={(integration, p, s, isApiProvider) =>
                        isApiProvider
                          ? resolvers.create.configure.operations({
                              integration,
                            })
                          : resolvers.create.finish.selectStep({
                              ...params,
                              integration,
                              position: '1',
                            })
                      }
                      getBreadcrumb={getBreadcrumb}
                    />
                  )}
                </WithLeaveConfirmation>
              </Route>

              {/* finish step */}
              <Route path={routes.create.finish.selectStep}>
                <WithLeaveConfirmation
                  i18nTitle={i18nTitle}
                  i18nConfirmationMessage={i18nConfirmationMessage}
                  shouldDisplayDialog={shouldDisplayDialog}
                >
                  {() => (
                    <EditorApp
                      mode={'adding'}
                      appStepRoutes={routes.create.finish}
                      appResolvers={resolvers.create.finish}
                      cancelHref={resolvers.list}
                      postConfigureHref={(integration, p) =>
                        resolvers.create.configure.index({
                          ...p,
                          integration,
                        })
                      }
                      getBreadcrumb={getBreadcrumb}
                    />
                  )}
                </WithLeaveConfirmation>
              </Route>

              <Route path={routes.create.configure.index} exact={true}>
                <WithLeaveConfirmation
                  i18nTitle={i18nTitle}
                  i18nConfirmationMessage={i18nConfirmationMessage}
                  shouldDisplayDialog={shouldDisplayDialog}
                >
                  {() => (
                    <AddStepPage
                      cancelHref={resolvers.list}
                      getAddStepHref={(position, p, s) =>
                        resolvers.create.configure.addStep.selectStep({
                          position: `${position}`,
                          ...p,
                          ...s,
                        })
                      }
                      getDeleteEdgeStepHref={(position, p, s) =>
                        resolvers.create.configure.editStep.selectStep({
                          position: `${position}`,
                          ...p,
                          ...s,
                        })
                      }
                      apiProviderHref={(step, p, s) =>
                        resolvers.create.start.apiProvider.selectMethod({
                          ...p,
                          ...s,
                        })
                      }
                      connectionHref={(step, p, s) =>
                        resolvers.create.configure.editStep.connection.configureAction(
                          {
                            actionId: step.action!.id!,
                            connection: step.connection!,
                            ...p,
                            ...s,
                          }
                        )
                      }
                      filterHref={(step, p, s) =>
                        resolvers.create.configure.editStep.basicFilter({
                          step,
                          ...p,
                          ...s,
                        })
                      }
                      choiceHref={(step, p, s) => {
                        const configMode = getChoiceConfigMode(step);
                        if (typeof configMode !== 'undefined') {
                          return resolvers.create.configure.editStep.choice.configure({
                            configMode,
                            step,
                            ...p,
                            ...s,
                          })
                        } else {
                          return resolvers.create.configure.editStep.choice.selectMode({
                            step,
                            ...p,
                            ...s,
                          })
                        }
                      }}
                      getAddMapperStepHref={(position, p, s) =>
                        resolvers.create.configure.addStep.dataMapper({
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
                        resolvers.create.configure.editStep.dataMapper({
                          step,
                          ...p,
                          ...s,
                        })
                      }
                      templateHref={(step, p, s) =>
                        resolvers.create.configure.editStep.template({
                          step,
                          ...p,
                          ...s,
                        })
                      }
                      stepHref={(step, p, s) =>
                        resolvers.create.configure.editStep.step({
                          step,
                          ...p,
                          ...s,
                        })
                      }
                      saveHref={(p, s) =>
                        resolvers.create.configure.saveAndPublish({
                          ...p,
                          ...s,
                        })
                      }
                      selfHref={(p, s) =>
                        resolvers.create.configure.index({
                          ...p,
                          ...s,
                        })
                      }
                      getBreadcrumb={getBreadcrumb}
                      getFlowHref={(flowId, p, s) =>
                        resolvers.create.configure.index({ ...p, ...s, flowId })
                      }
                      getGotoDescribeDataHref={(position, flowId, p, s) => {
                        const step = getStep(
                          state.integration,
                          flowId,
                          position
                        ) as StepKind;
                        return resolvers.create.configure.editStep.connection.describeData(
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

              <Route path={routes.create.configure.operations} exact={true}>
                <WithLeaveConfirmation
                  i18nTitle={i18nTitle}
                  i18nConfirmationMessage={i18nConfirmationMessage}
                  shouldDisplayDialog={shouldDisplayDialog}
                >
                  {() => (
                    <OperationsPage
                      cancelHref={(p, s) => resolvers.list()}
                      saveHref={(p, s) =>
                        resolvers.create.configure.saveAndPublish({
                          ...p,
                          ...s,
                        })
                      }
                      getFlowHref={(p, s) =>
                        resolvers.create.configure.index({ ...p, ...s })
                      }
                      getBreadcrumb={getBreadcrumb}
                    />
                  )}
                </WithLeaveConfirmation>
              </Route>

              {/* add step */}
              <Route path={routes.create.configure.addStep.selectStep}>
                <WithLeaveConfirmation
                  i18nTitle={i18nTitle}
                  i18nConfirmationMessage={i18nConfirmationMessage}
                  shouldDisplayDialog={shouldDisplayDialog}
                >
                  {() => (
                    <EditorApp
                      mode={'adding'}
                      appStepRoutes={routes.create.configure.addStep}
                      appResolvers={resolvers.create.configure.addStep}
                      cancelHref={(p, s) =>
                        resolvers.create.configure.index({
                          ...p,
                          ...s,
                        })
                      }
                      postConfigureHref={(integration, p) =>
                        resolvers.create.configure.index({
                          ...p,
                          integration,
                        })
                      }
                      getBreadcrumb={getBreadcrumb}
                    />
                  )}
                </WithLeaveConfirmation>
              </Route>

              {/* edit step */}
              <Route path={routes.create.configure.editStep.selectStep}>
                <WithLeaveConfirmation
                  i18nTitle={i18nTitle}
                  i18nConfirmationMessage={i18nConfirmationMessage}
                  shouldDisplayDialog={shouldDisplayDialog}
                >
                  {() => (
                    <EditorApp
                      mode={'editing'}
                      appStepRoutes={routes.create.configure.editStep}
                      appResolvers={resolvers.create.configure.editStep}
                      cancelHref={(p, s) =>
                        resolvers.create.configure.index({
                          ...p,
                          ...s,
                        })
                      }
                      postConfigureHref={(integration, p) =>
                        resolvers.create.configure.index({
                          ...p,
                          integration,
                        })
                      }
                      getBreadcrumb={getBreadcrumb}
                    />
                  )}
                </WithLeaveConfirmation>
              </Route>

              <Route path={routes.create.configure.saveAndPublish} exact={true}>
                <SaveIntegrationPage
                  cancelHref={(p, s) =>
                    resolvers.create.configure.entryPoint({ ...p, ...s })
                  }
                  postSaveHref={(p, s) =>
                    resolvers.integration.edit.entryPoint({
                      ...p,
                      ...s,
                    })
                  }
                  postPublishHref={p => resolvers.integration.details({ ...p })}
                  i18nTitle={i18nTitle}
                  i18nConfirmationMessage={i18nConfirmationMessage}
                  shouldDisplayDialog={shouldDisplayDialog}
                  getBreadcrumb={getBreadcrumb}
                />
              </Route>
            </Switch>
          );
        }}
      </Translation>
    </WithClosedNavigation>
  );
};
