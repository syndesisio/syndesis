/* tslint:disable:object-literal-sort-keys */
import { getConnectionIcon } from '@syndesis/api';
import { Integration } from '@syndesis/models';
import { Breadcrumb } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Route, Switch } from 'react-router';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../shared';
import { IntegrationEditorSidebar } from './components';
import { AddStepPage } from './components/editor/AddStepPage';
import { ReviewPage } from './components/editor/api-provider/EditPage';
import { EditPage } from './components/editor/api-provider/ReviewPage';
import { UploadPage } from './components/editor/api-provider/UploadPage';
import { EditorApp } from './components/editor/EditorApp';
import { ConfigureActionPage } from './components/editor/endpoint/ConfigureActionPage';
import { SelectActionPage } from './components/editor/endpoint/SelectActionPage';
import { SaveIntegrationPage } from './components/editor/SaveIntegrationPage';
import { SelectConnectionPage } from './components/editor/SelectConnectionPage';
import resolvers from './resolvers';
import routes from './routes';

const addStepPage = (
  <AddStepPage
    cancelHref={resolvers.list}
    getEditAddStepHref={(position, p, s) =>
      resolvers.integration.edit.addStep.selectStep({
        position: `${position}`,
        ...p,
        ...s,
      })
    }
    apiProviderHref={resolvers.integration.edit.editStep.apiProvider.review}
    connectionHref={(step, params, state) =>
      resolvers.integration.edit.editStep.connection.configureAction({
        actionId: step.action!.id!,
        connection: step.connection!,
        ...params,
        ...state,
      })
    }
    filterHref={resolvers.integration.edit.editStep.basicFilter}
    extensionHref={resolvers.integration.edit.editStep.extension}
    mapperHref={resolvers.integration.edit.editStep.dataMapper}
    templateHref={resolvers.integration.edit.editStep.template}
    stepHref={resolvers.integration.edit.editStep.step}
    saveHref={(p, s) =>
      resolvers.integration.edit.saveAndPublish({
        ...p,
        ...s,
      })
    }
  />
);

const selectConnectionPage = (
  <SelectConnectionPage
    cancelHref={(p, s) => resolvers.integration.edit.index({ ...p, ...s })}
    apiProviderHref={(p, s) => ({ pathname: 'todo' })}
    connectionHref={(connection, p, s) =>
      resolvers.integration.edit.addStep.connection.selectAction({
        connection,
        ...p,
        ...s,
      })
    }
    filterHref={resolvers.integration.edit.addStep.basicFilter}
    extensionHref={resolvers.integration.edit.addStep.extension}
    mapperHref={resolvers.integration.edit.addStep.dataMapper}
    templateHref={resolvers.integration.edit.addStep.template}
    stepHref={resolvers.integration.edit.addStep.step}
    sidebar={({ steps, activeIndex }) => (
      <IntegrationEditorSidebar
        steps={steps}
        addAtIndex={activeIndex}
        addI18nTitle={`${activeIndex + 1}. Start`}
        addI18nTooltip={'Start'}
        addI18nDescription={'Choose a connection'}
      />
    )}
  />
);

const saveIntegrationPage = (
  <SaveIntegrationPage
    cancelHref={(p, s) => resolvers.integration.edit.index({ ...p, ...s })}
    postSaveHref={resolvers.list}
  />
);

const addStepSelectActionPage = (
  <SelectActionPage
    cancelHref={(p, s) => resolvers.integration.edit.index({ ...p, ...s })}
    sidebar={({ connection, steps, activeIndex }) => (
      <IntegrationEditorSidebar
        steps={steps}
        addAtIndex={activeIndex}
        addIcon={
          <img
            src={getConnectionIcon(process.env.PUBLIC_URL, connection)}
            height={24}
            width={24}
          />
        }
        addI18nTitle={`${activeIndex + 1}. ${connection.connector!.name}`}
        addI18nTooltip={`${activeIndex + 1}. ${connection.name}`}
        addI18nDescription={'Choose an action'}
      />
    )}
    selectHref={(actionId, p, s) =>
      resolvers.integration.edit.addStep.connection.configureAction({
        actionId,
        ...p,
        ...s,
      })
    }
  />
);

const addStepConfigureActionPage = (
  <ConfigureActionPage
    backHref={(p, s) =>
      resolvers.integration.edit.addStep.connection.selectAction({ ...p, ...s })
    }
    cancelHref={(p, s) => resolvers.integration.edit.index({ ...p, ...s })}
    nextStepHref={(p, s) =>
      resolvers.integration.edit.addStep.connection.configureAction({
        ...p,
        ...s,
      })
    }
    mode={'adding'}
    sidebar={({ connection, steps, activeIndex }) => (
      <IntegrationEditorSidebar
        steps={steps}
        addAtIndex={activeIndex}
        addIcon={
          <img
            src={getConnectionIcon(process.env.PUBLIC_URL, connection)}
            height={24}
            width={24}
          />
        }
        addI18nTitle={`${activeIndex + 1}. ${connection.connector!.name}`}
        addI18nTooltip={`${activeIndex + 1}. ${connection.name}`}
        addI18nDescription={'Configure the action'}
      />
    )}
    postConfigureHref={(integration, params) =>
      resolvers.integration.edit.index({
        integration,
        ...params,
      })
    }
  />
);

const editStepSelectActionPage = (
  <SelectActionPage
    cancelHref={(p, s) => resolvers.integration.edit.index({ ...p, ...s })}
    sidebar={({ steps, activeIndex }) => (
      <IntegrationEditorSidebar steps={steps} activeIndex={activeIndex} />
    )}
    selectHref={(actionId, p, s) =>
      resolvers.integration.edit.editStep.connection.configureAction({
        actionId,
        ...p,
        ...s,
      })
    }
  />
);

const editStepConfigureActionPage = (
  <ConfigureActionPage
    backHref={(p, s) =>
      resolvers.integration.edit.editStep.connection.selectAction({
        ...p,
        ...s,
      })
    }
    cancelHref={(p, s) => resolvers.integration.edit.index({ ...p, ...s })}
    mode={'editing'}
    nextStepHref={(p, s) =>
      resolvers.integration.edit.editStep.connection.configureAction({
        ...p,
        ...s,
      })
    }
    sidebar={({ steps, activeIndex }) => (
      <IntegrationEditorSidebar steps={steps} activeIndex={activeIndex} />
    )}
    postConfigureHref={(integration, params) =>
      resolvers.integration.edit.index({
        integration,
        ...params,
      })
    }
  />
);

export interface IIntegrationEditorAppRouteState {
  integration: Integration;
}

const TODO: React.FunctionComponent = () => <>TODO</>;

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
 * @todo i18n everywhere!
 */
export const IntegrationEditorApp: React.FunctionComponent = () => {
  return (
    <WithRouteData<null, IIntegrationEditorAppRouteState>>
      {(_, { integration }) => (
        <WithClosedNavigation>
          <Breadcrumb>
            <Link to={resolvers.list()}>Integrations</Link>
            <Link
              to={resolvers.integration.details({
                integrationId: integration.id!,
              })}
            >
              {integration.name}
            </Link>
            <span>Add to integration</span>
          </Breadcrumb>
          <Switch>
            <Route
              path={routes.integration.edit.index}
              exact={true}
              children={addStepPage}
            />

            {/* add step */}
            <Route path={routes.integration.edit.addStep.selectStep}>
              <EditorApp
                selectStepPath={routes.integration.edit.addStep.selectStep}
                selectStepChildren={selectConnectionPage}
                endpointEditor={{
                  selectActionPath:
                    routes.integration.edit.addStep.connection.selectAction,
                  selectActionChildren: addStepSelectActionPage,
                  configureActionPath:
                    routes.integration.edit.addStep.connection.configureAction,
                  configureActionChildren: addStepConfigureActionPage,
                  describeDataPath:
                    routes.integration.edit.addStep.connection.describeData,
                  describeDataChildren: TODO,
                }}
                apiProvider={{
                  uploadPath:
                    routes.integration.edit.addStep.apiProvider.upload,
                  uploadChildren: <UploadPage />,
                  reviewPath:
                    routes.integration.edit.addStep.apiProvider.review,
                  reviewChildren: <ReviewPage />,
                  editPath: routes.integration.edit.addStep.apiProvider.edit,
                  editChildren: <EditPage />,
                }}
                template={{
                  templatePath: routes.integration.edit.addStep.template,
                  templateChildren: TODO,
                }}
                dataMapper={{
                  mapperPath: routes.integration.edit.addStep.dataMapper,
                  mapperChildren: TODO,
                }}
                basicFilter={{
                  filterPath: routes.integration.edit.addStep.basicFilter,
                  filterChildren: TODO,
                }}
                step={{
                  configurePath: routes.integration.edit.addStep.step,
                  configureChildren: TODO,
                }}
                extension={{
                  configurePath: routes.integration.edit.addStep.step,
                  configureChildren: TODO,
                }}
              />
            </Route>

            {/* edit step */}
            <Route path={routes.integration.edit.editStep.selectStep}>
              <EditorApp
                endpointEditor={{
                  selectActionPath:
                    routes.integration.edit.editStep.connection.selectAction,
                  selectActionChildren: editStepSelectActionPage,
                  configureActionPath:
                    routes.integration.edit.editStep.connection.configureAction,
                  configureActionChildren: editStepConfigureActionPage,
                  describeDataPath:
                    routes.integration.edit.editStep.connection.describeData,
                  describeDataChildren: TODO,
                }}
                apiProvider={{
                  uploadPath:
                    routes.integration.edit.editStep.apiProvider.upload,
                  uploadChildren: <UploadPage />,
                  reviewPath:
                    routes.integration.edit.editStep.apiProvider.review,
                  reviewChildren: <ReviewPage />,
                  editPath: routes.integration.edit.editStep.apiProvider.edit,
                  editChildren: <EditPage />,
                }}
                template={{
                  templatePath: routes.integration.edit.editStep.template,
                  templateChildren: TODO,
                }}
                dataMapper={{
                  mapperPath: routes.integration.edit.editStep.dataMapper,
                  mapperChildren: TODO,
                }}
                basicFilter={{
                  filterPath: routes.integration.edit.editStep.basicFilter,
                  filterChildren: TODO,
                }}
                step={{
                  configurePath: routes.integration.edit.editStep.step,
                  configureChildren: TODO,
                }}
                extension={{
                  configurePath: routes.integration.edit.editStep.extension,
                  configureChildren: TODO,
                }}
              />
            </Route>

            <Route
              path={routes.integration.edit.saveAndPublish}
              exact={true}
              children={saveIntegrationPage}
            />
          </Switch>
        </WithClosedNavigation>
      )}
    </WithRouteData>
  );
};
