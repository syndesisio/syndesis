import { ALL_STEPS, createStep, DATA_MAPPER } from '@syndesis/api';
import * as H from '@syndesis/history';
import { StepKind } from '@syndesis/models';
import { Breadcrumb } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Route, Switch } from 'react-router';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../shared';
import { WithLeaveConfirmation } from '../../shared/WithLeaveConfirmation';
import { AddStepPage } from './components/editor/AddStepPage';
import { EditorApp } from './components/editor/EditorApp';
import { SaveIntegrationPage } from './components/editor/SaveIntegrationPage';
import resolvers from './resolvers';
import routes from './routes';

const addStepPage = (
  <AddStepPage
    cancelHref={resolvers.list}
    getEditAddStepHref={(position, p, s) =>
      resolvers.create.configure.addStep.selectStep({
        position: `${position}`,
        ...p,
        ...s,
      })
    }
    apiProviderHref={(step, p, s) =>
      resolvers.create.configure.editStep.apiProvider.review()
    }
    connectionHref={(step, params, state) =>
      resolvers.create.configure.editStep.connection.configureAction({
        actionId: step.action!.id!,
        connection: step.connection!,
        ...params,
        ...state,
      })
    }
    filterHref={resolvers.create.configure.editStep.basicFilter}
    getAddMapperStepHref={(position, params, state) =>
      resolvers.create.configure.addStep.dataMapper({
        position: `${position}`,
        step: {
          ...createStep(),
          ...ALL_STEPS.find(s => s.stepKind === DATA_MAPPER),
        } as StepKind,
        ...params,
        ...state,
      })
    }
    mapperHref={(step, params, state) =>
      resolvers.create.configure.editStep.dataMapper({
        step,
        ...params,
        ...state,
      })
    }
    templateHref={(step, params, state) =>
      resolvers.create.configure.editStep.template({
        step,
        ...params,
        ...state,
      })
    }
    stepHref={(step, params, state) =>
      resolvers.create.configure.editStep.step({
        step,
        ...params,
        ...state,
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
  />
);

const saveIntegrationPage = (
  <SaveIntegrationPage
    cancelHref={(p, s) => resolvers.create.configure.index({ ...p, ...s })}
    postSaveHref={(p, s) =>
      resolvers.integration.edit.index({
        ...p,
        ...s,
      })
    }
    postPublishHref={p => resolvers.integration.details({ ...p })}
  />
);

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
  return (
    <WithClosedNavigation>
      <Breadcrumb>
        <Link
          data-testid={'integration-creator-app-new-integration'}
          to={resolvers.list()}
        >
          Integrations
        </Link>
        <span>New integration</span>
      </Breadcrumb>
      <Translation ns={['integrations']}>
        {t => (
          <WithLeaveConfirmation
            i18nTitle={t('unsavedChangesTitle')}
            i18nConfirmationMessage={t('unsavedChangesMessage')}
            shouldDisplayDialog={(location: H.LocationDescriptor) => {
              const url =
                typeof location === 'string' ? location : location.pathname!;
              return !url.startsWith(routes.create.root);
            }}
          >
            {() => (
              <Switch>
                {/* start step */}
                <Route path={routes.create.start.selectStep}>
                  <EditorApp
                    mode={'adding'}
                    appStepRoutes={routes.create.start}
                    appResolvers={resolvers.create.start}
                    cancelHref={resolvers.list}
                    postConfigureHref={(integration, params) => {
                      return resolvers.create.finish.selectStep({
                        integration,
                        ...params,
                        position: '1',
                      });
                    }}
                  />
                </Route>

                {/* finish step */}
                <Route path={routes.create.finish.selectStep}>
                  <EditorApp
                    mode={'adding'}
                    appStepRoutes={routes.create.finish}
                    appResolvers={resolvers.create.finish}
                    cancelHref={resolvers.list}
                    postConfigureHref={(integration, params) =>
                      resolvers.create.configure.index({
                        integration,
                        ...params,
                      })
                    }
                  />
                </Route>

                <Route
                  path={routes.create.configure.index}
                  exact={true}
                  children={addStepPage}
                />

                {/* add step */}
                <Route path={routes.create.configure.addStep.selectStep}>
                  <EditorApp
                    mode={'adding'}
                    appStepRoutes={routes.create.configure.addStep}
                    appResolvers={resolvers.create.configure.addStep}
                    cancelHref={(params, state) =>
                      resolvers.create.configure.index({
                        ...params,
                        ...state,
                      })
                    }
                    postConfigureHref={(integration, params) =>
                      resolvers.create.configure.index({
                        integration,
                        ...params,
                      })
                    }
                  />
                </Route>

                {/* edit step */}
                <Route path={routes.create.configure.editStep.selectStep}>
                  <EditorApp
                    mode={'editing'}
                    appStepRoutes={routes.create.configure.editStep}
                    appResolvers={resolvers.create.configure.editStep}
                    cancelHref={(params, state) =>
                      resolvers.create.configure.index({
                        ...params,
                        ...state,
                      })
                    }
                    postConfigureHref={(integration, params) =>
                      resolvers.create.configure.index({
                        integration,
                        ...params,
                      })
                    }
                  />
                </Route>

                <Route
                  path={routes.create.configure.saveAndPublish}
                  exact={true}
                  children={saveIntegrationPage}
                />
              </Switch>
            )}
          </WithLeaveConfirmation>
        )}
      </Translation>
    </WithClosedNavigation>
  );
};
