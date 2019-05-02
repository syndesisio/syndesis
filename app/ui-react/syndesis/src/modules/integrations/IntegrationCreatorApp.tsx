/* tslint:disable:object-literal-sort-keys */
import { getConnectionIcon } from '@syndesis/api';
import {
  Breadcrumb,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
} from '@syndesis/ui';
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
      resolvers.create.configure.addStep.selectStep({
        position: `${position}`,
        ...p,
        ...s,
      })
    }
    apiProviderHref={resolvers.create.configure.editStep.apiProvider.review}
    connectionHref={(step, params, state) =>
      resolvers.create.configure.editStep.connection.configureAction({
        actionId: step.action!.id!,
        connection: step.connection!,
        ...params,
        ...state,
      })
    }
    filterHref={resolvers.create.configure.editStep.basicFilter}
    extensionHref={resolvers.create.configure.editStep.extension}
    mapperHref={resolvers.create.configure.editStep.dataMapper}
    templateHref={resolvers.create.configure.editStep.template}
    stepHref={resolvers.create.configure.editStep.step}
    saveHref={(p, s) =>
      resolvers.create.configure.saveAndPublish({
        ...p,
        ...s,
      })
    }
  />
);

const saveIntegrationPage = (
  <SaveIntegrationPage
    cancelHref={(p, s) => resolvers.create.configure.index({ ...p, ...s })}
    postSaveHref={resolvers.list}
  />
);

const startStepSelectConnectionPage = (
  <SelectConnectionPage
    cancelHref={resolvers.list}
    apiProviderHref={resolvers.create.start.apiProvider.upload}
    connectionHref={(connection, params, state) =>
      resolvers.create.start.connection.selectAction({
        connection,
        ...params,
        ...state,
      })
    }
    filterHref={resolvers.create.start.basicFilter}
    extensionHref={resolvers.create.start.extension}
    mapperHref={resolvers.create.start.dataMapper}
    templateHref={resolvers.create.start.template}
    stepHref={resolvers.create.start.step}
    sidebar={() => (
      <IntegrationVerticalFlow>
        {({ expanded }) => (
          <>
            <IntegrationFlowStepGeneric
              icon={<i className={'fa fa-plus'} />}
              i18nTitle={'1. Start'}
              i18nTooltip={'Start'}
              active={true}
              showDetails={expanded}
              description={'Choose a connection'}
            />
            <IntegrationFlowStepWithOverview
              icon={<i className={'fa fa-plus'} />}
              i18nTitle={'2. Finish'}
              i18nTooltip={'Finish'}
              active={false}
              showDetails={expanded}
              name={'n/a'}
              action={'n/a'}
              dataType={'n/a'}
            />
          </>
        )}
      </IntegrationVerticalFlow>
    )}
  />
);

const startStepSelectActionPage = (
  <SelectActionPage
    cancelHref={resolvers.create.start.selectStep}
    sidebar={({ connection }) => (
      <IntegrationVerticalFlow>
        {({ expanded }) => (
          <>
            <IntegrationFlowStepGeneric
              icon={
                <img
                  src={getConnectionIcon(process.env.PUBLIC_URL, connection)}
                  width={24}
                  height={24}
                />
              }
              i18nTitle={`1. ${connection.connector!.name}`}
              i18nTooltip={`1. ${connection.name}`}
              active={true}
              showDetails={expanded}
              description={'Choose an action'}
            />
            <IntegrationFlowStepWithOverview
              icon={<i className={'fa fa-plus'} />}
              i18nTitle={'2. Finish'}
              i18nTooltip={'Finish'}
              active={false}
              showDetails={expanded}
              name={'n/a'}
              action={'n/a'}
              dataType={'n/a'}
            />
          </>
        )}
      </IntegrationVerticalFlow>
    )}
    selectHref={(actionId, p, s) =>
      resolvers.create.start.connection.configureAction({
        actionId,
        ...p,
        ...s,
      })
    }
  />
);

const startStepConfigureActionPage = (
  <ConfigureActionPage
    backHref={(p, s) =>
      resolvers.create.start.connection.selectAction({ ...p, ...s })
    }
    cancelHref={resolvers.list}
    mode={'adding'}
    nextStepHref={(p, s) =>
      resolvers.create.start.connection.configureAction({
        ...p,
        ...s,
      })
    }
    sidebar={({ connection }) => (
      <IntegrationVerticalFlow>
        {({ expanded }) => (
          <>
            <IntegrationFlowStepGeneric
              icon={
                <img
                  src={getConnectionIcon(process.env.PUBLIC_URL, connection)}
                  width={24}
                  height={24}
                />
              }
              i18nTitle={`1. ${connection.connector!.name}`}
              i18nTooltip={`1. ${connection.name}`}
              active={true}
              showDetails={expanded}
              description={'Configure the action'}
            />
            <IntegrationFlowStepWithOverview
              icon={<i className={'fa fa-plus'} />}
              i18nTitle={'2. Finish'}
              i18nTooltip={'Finish'}
              active={false}
              showDetails={expanded}
              name={'n/a'}
              action={'n/a'}
              dataType={'n/a'}
            />
          </>
        )}
      </IntegrationVerticalFlow>
    )}
    postConfigureHref={(integration, params, state) => {
      return resolvers.create.finish.selectStep({
        integration,
        ...params,
        position: '1',
      });
    }}
  />
);

const finishStepSelectConnectionPage = (
  <SelectConnectionPage
    cancelHref={resolvers.list}
    apiProviderHref={resolvers.create.finish.apiProvider.upload}
    connectionHref={(connection, params, state) =>
      resolvers.create.finish.connection.selectAction({
        connection,
        ...params,
        ...state,
      })
    }
    filterHref={resolvers.create.finish.basicFilter}
    extensionHref={resolvers.create.finish.extension}
    mapperHref={resolvers.create.finish.dataMapper}
    templateHref={resolvers.create.finish.template}
    stepHref={resolvers.create.finish.step}
    sidebar={({ steps }) => (
      <IntegrationVerticalFlow>
        {({ expanded }) => {
          const startAction = steps[0];
          return (
            <>
              <IntegrationFlowStepWithOverview
                icon={
                  <img
                    src={getConnectionIcon(
                      process.env.PUBLIC_URL,
                      startAction.connection!
                    )}
                    width={24}
                    height={24}
                  />
                }
                i18nTitle={`1. ${startAction.connection!.name}`}
                i18nTooltip={`1. ${startAction.connection!.name}`}
                active={false}
                showDetails={expanded}
                name={startAction.connection!.connector!.name}
                action={startAction.connection!.name!}
                dataType={'TODO'}
              />
              <IntegrationFlowStepGeneric
                icon={<i className={'fa fa-plus'} />}
                i18nTitle={'2. Finish'}
                i18nTooltip={'Finish'}
                active={true}
                showDetails={expanded}
                description={'Choose a connection'}
              />
            </>
          );
        }}
      </IntegrationVerticalFlow>
    )}
  />
);

const finishStepSelectActionPage = (
  <SelectActionPage
    cancelHref={(p, s) => resolvers.create.finish.selectStep({ ...p, ...s })}
    sidebar={({ connection, steps }) => (
      <IntegrationVerticalFlow>
        {({ expanded }) => {
          const startAction = steps[0];
          return (
            <>
              <IntegrationFlowStepWithOverview
                icon={
                  <img
                    src={getConnectionIcon(
                      process.env.PUBLIC_URL,
                      startAction.connection!
                    )}
                    width={24}
                    height={24}
                  />
                }
                i18nTitle={`1. ${startAction.connection!.name}`}
                i18nTooltip={`1. ${startAction.connection!.name}`}
                active={false}
                showDetails={expanded}
                name={startAction.connection!.connector!.name}
                action={startAction.connection!.name!}
                dataType={'TODO'}
              />
              <IntegrationFlowStepGeneric
                icon={
                  <img
                    src={getConnectionIcon(process.env.PUBLIC_URL, connection)}
                    width={24}
                    height={24}
                  />
                }
                i18nTitle={`2. ${connection.connector!.name}`}
                i18nTooltip={`2. ${connection.name}`}
                active={true}
                showDetails={expanded}
                description={'Choose an action'}
              />
            </>
          );
        }}
      </IntegrationVerticalFlow>
    )}
    selectHref={(actionId, p, s) =>
      resolvers.create.finish.connection.configureAction({
        actionId,
        ...p,
        ...s,
      })
    }
  />
);

const finishStepConfigureActionPage = (
  <ConfigureActionPage
    backHref={(p, s) =>
      resolvers.create.finish.connection.selectAction({ ...p, ...s })
    }
    cancelHref={resolvers.list}
    mode={'adding'}
    nextStepHref={(p, s) =>
      resolvers.create.finish.connection.configureAction({
        ...p,
        ...s,
      })
    }
    sidebar={({ connection, steps }) => (
      <IntegrationVerticalFlow>
        {({ expanded }) => {
          const startAction = steps[0];
          return (
            <>
              <IntegrationFlowStepWithOverview
                icon={
                  <img
                    src={getConnectionIcon(
                      process.env.PUBLIC_URL,
                      startAction.connection!
                    )}
                    width={24}
                    height={24}
                  />
                }
                i18nTitle={`1. ${startAction.connection!.name}`}
                i18nTooltip={`1. ${startAction.connection!.name}`}
                active={false}
                showDetails={expanded}
                name={startAction.connection!.connector!.name}
                action={startAction.connection!.name!}
                dataType={'TODO'}
              />
              <IntegrationFlowStepGeneric
                icon={
                  <img
                    src={getConnectionIcon(process.env.PUBLIC_URL, connection)}
                    width={24}
                    height={24}
                  />
                }
                i18nTitle={`2. ${connection.connector!.name}`}
                i18nTooltip={`2. ${connection.name}`}
                active={true}
                showDetails={expanded}
                description={'Configure the action'}
              />
            </>
          );
        }}
      </IntegrationVerticalFlow>
    )}
    postConfigureHref={(integration, params) =>
      resolvers.create.configure.index({
        integration,
        ...params,
      })
    }
  />
);

const addStepSelectConnectionPage = (
  <SelectConnectionPage
    cancelHref={(p, s) => resolvers.create.configure.index({ ...p, ...s })}
    apiProviderHref={resolvers.create.configure.addStep.apiProvider.upload}
    connectionHref={(connection, p, s) =>
      resolvers.create.configure.addStep.connection.selectAction({
        connection,
        ...p,
        ...s,
      })
    }
    filterHref={resolvers.create.configure.addStep.basicFilter}
    extensionHref={resolvers.create.configure.addStep.extension}
    mapperHref={resolvers.create.configure.addStep.dataMapper}
    templateHref={resolvers.create.configure.addStep.template}
    stepHref={resolvers.create.configure.addStep.step}
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

const addStepSelectActionPage = (
  <SelectActionPage
    cancelHref={(p, s) => resolvers.create.configure.index({ ...p, ...s })}
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
      resolvers.create.configure.addStep.connection.configureAction({
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
      resolvers.create.configure.addStep.connection.selectAction({ ...p, ...s })
    }
    cancelHref={(p, s) => resolvers.create.configure.index({ ...p, ...s })}
    mode={'adding'}
    nextStepHref={(p, s) =>
      resolvers.create.configure.addStep.connection.configureAction({
        ...p,
        ...s,
      })
    }
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
      resolvers.create.configure.index({
        integration,
        ...params,
      })
    }
  />
);

const editStepSelectActionPage = (
  <SelectActionPage
    cancelHref={(p, s) => resolvers.create.configure.index({ ...p, ...s })}
    sidebar={({ steps, activeIndex }) => (
      <IntegrationEditorSidebar steps={steps} activeIndex={activeIndex} />
    )}
    selectHref={(actionId, p, s) =>
      resolvers.create.configure.editStep.connection.configureAction({
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
      resolvers.create.configure.editStep.connection.selectAction({
        ...p,
        ...s,
      })
    }
    cancelHref={(p, s) => resolvers.create.configure.index({ ...p, ...s })}
    mode={'editing'}
    nextStepHref={(p, s) =>
      resolvers.create.configure.editStep.connection.configureAction({
        ...p,
        ...s,
      })
    }
    sidebar={({ steps, activeIndex }) => (
      <IntegrationEditorSidebar steps={steps} activeIndex={activeIndex} />
    )}
    postConfigureHref={(integration, params) =>
      resolvers.create.configure.index({
        integration,
        ...params,
      })
    }
  />
);

const TODO: React.FunctionComponent = () => <>TODO</>;

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
        <Link to={resolvers.list()}>Integrations</Link>
        <span>New integration</span>
      </Breadcrumb>
      <Switch>
        {/* start step */}
        <Route path={routes.create.start.selectStep}>
          <EditorApp
            selectStepPath={routes.create.start.selectStep}
            selectStepChildren={startStepSelectConnectionPage}
            endpointEditor={{
              selectActionPath: routes.create.start.connection.selectAction,
              selectActionChildren: startStepSelectActionPage,
              configureActionPath:
                routes.create.start.connection.configureAction,
              configureActionChildren: startStepConfigureActionPage,
              describeDataPath: routes.create.start.connection.describeData,
              describeDataChildren: TODO,
            }}
            apiProvider={{
              uploadPath: routes.create.start.apiProvider.upload,
              uploadChildren: <UploadPage />,
              reviewPath: routes.create.start.apiProvider.review,
              reviewChildren: <ReviewPage />,
              editPath: routes.create.start.apiProvider.edit,
              editChildren: <EditPage />,
            }}
            template={{
              templatePath: routes.create.start.template,
              templateChildren: TODO,
            }}
            dataMapper={{
              mapperPath: routes.create.start.dataMapper,
              mapperChildren: TODO,
            }}
            basicFilter={{
              filterPath: routes.create.start.basicFilter,
              filterChildren: TODO,
            }}
            step={{
              configurePath: routes.create.start.step,
              configureChildren: TODO,
            }}
            extension={{
              configurePath: routes.create.start.extension,
              configureChildren: TODO,
            }}
          />
        </Route>

        {/* finish step */}
        <Route path={routes.create.finish.selectStep}>
          <EditorApp
            selectStepPath={routes.create.finish.selectStep}
            selectStepChildren={finishStepSelectConnectionPage}
            endpointEditor={{
              selectActionPath: routes.create.finish.connection.selectAction,
              selectActionChildren: finishStepSelectActionPage,
              configureActionPath:
                routes.create.finish.connection.configureAction,
              configureActionChildren: finishStepConfigureActionPage,
              describeDataPath: routes.create.finish.connection.describeData,
              describeDataChildren: TODO,
            }}
            apiProvider={{
              uploadPath: routes.create.finish.apiProvider.upload,
              uploadChildren: <UploadPage />,
              reviewPath: routes.create.finish.apiProvider.review,
              reviewChildren: <ReviewPage />,
              editPath: routes.create.finish.apiProvider.edit,
              editChildren: <EditPage />,
            }}
            template={{
              templatePath: routes.create.finish.template,
              templateChildren: TODO,
            }}
            dataMapper={{
              mapperPath: routes.create.finish.dataMapper,
              mapperChildren: TODO,
            }}
            basicFilter={{
              filterPath: routes.create.finish.basicFilter,
              filterChildren: TODO,
            }}
            step={{
              configurePath: routes.create.finish.step,
              configureChildren: TODO,
            }}
            extension={{
              configurePath: routes.create.finish.extension,
              configureChildren: TODO,
            }}
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
            selectStepPath={routes.create.configure.addStep.selectStep}
            selectStepChildren={addStepSelectConnectionPage}
            endpointEditor={{
              selectActionPath:
                routes.create.configure.addStep.connection.selectAction,
              selectActionChildren: addStepSelectActionPage,
              configureActionPath:
                routes.create.configure.addStep.connection.configureAction,
              configureActionChildren: addStepConfigureActionPage,
              describeDataPath:
                routes.create.configure.addStep.connection.describeData,
              describeDataChildren: TODO,
            }}
            apiProvider={{
              uploadPath: routes.create.configure.addStep.apiProvider.upload,
              uploadChildren: <UploadPage />,
              reviewPath: routes.create.configure.addStep.apiProvider.review,
              reviewChildren: <ReviewPage />,
              editPath: routes.create.configure.addStep.apiProvider.edit,
              editChildren: <EditPage />,
            }}
            template={{
              templatePath: routes.create.configure.addStep.template,
              templateChildren: TODO,
            }}
            dataMapper={{
              mapperPath: routes.create.configure.addStep.dataMapper,
              mapperChildren: TODO,
            }}
            basicFilter={{
              filterPath: routes.create.configure.addStep.basicFilter,
              filterChildren: TODO,
            }}
            step={{
              configurePath: routes.create.configure.addStep.step,
              configureChildren: TODO,
            }}
            extension={{
              configurePath: routes.create.configure.addStep.extension,
              configureChildren: TODO,
            }}
          />
        </Route>

        {/* edit step */}
        <Route path={routes.create.configure.editStep.selectStep}>
          <EditorApp
            endpointEditor={{
              selectActionPath:
                routes.create.configure.editStep.connection.selectAction,
              selectActionChildren: editStepSelectActionPage,
              configureActionPath:
                routes.create.configure.editStep.connection.configureAction,
              configureActionChildren: editStepConfigureActionPage,
              describeDataPath:
                routes.create.configure.editStep.connection.describeData,
              describeDataChildren: TODO,
            }}
            apiProvider={{
              uploadPath: routes.create.configure.editStep.apiProvider.upload,
              uploadChildren: <UploadPage />,
              reviewPath: routes.create.configure.editStep.apiProvider.review,
              reviewChildren: <ReviewPage />,
              editPath: routes.create.configure.editStep.apiProvider.edit,
              editChildren: <EditPage />,
            }}
            template={{
              templatePath: routes.create.configure.editStep.template,
              templateChildren: TODO,
            }}
            dataMapper={{
              mapperPath: routes.create.configure.editStep.dataMapper,
              mapperChildren: TODO,
            }}
            basicFilter={{
              filterPath: routes.create.configure.editStep.basicFilter,
              filterChildren: TODO,
            }}
            step={{
              configurePath: routes.create.configure.editStep.step,
              configureChildren: TODO,
            }}
            extension={{
              configurePath: routes.create.configure.editStep.extension,
              configureChildren: TODO,
            }}
          />
        </Route>

        <Route
          path={routes.create.configure.saveAndPublish}
          exact={true}
          children={saveIntegrationPage}
        />
      </Switch>
    </WithClosedNavigation>
  );
};
