import {
  getConnectionIcon,
  getEmptyIntegration,
  getSteps,
} from '@syndesis/api';
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
import {
  IntegrationCreatorBreadcrumbs,
  IntegrationEditorSidebar,
} from './components';
import { AddStepPage } from './components/editor/AddStepPage';
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
    getEditConfigureStepHrefCallback={(stepIdx, step, p, s) =>
      resolvers.create.configure.editStep.connection.configureAction({
        actionId: step.action!.id!,
        connection: step.connection!,
        position: `${stepIdx}`,
        ...p,
        ...s,
      })
    }
    header={<IntegrationCreatorBreadcrumbs step={3} />}
    nextHref={(p, s) =>
      resolvers.create.configure.saveAndPublish({
        ...p,
        ...s,
      })
    }
  />
);

const saveIntegrationPage = (
  <SaveIntegrationPage
    backHref={(p, s) => resolvers.create.configure.index({ ...p, ...s })}
    cancelHref={resolvers.list}
    header={<IntegrationCreatorBreadcrumbs step={4} />}
    postSaveHref={resolvers.list}
  />
);

const startStepSelectConnectionPage = (
  <SelectConnectionPage
    cancelHref={resolvers.list}
    header={<IntegrationCreatorBreadcrumbs step={1} />}
    apiProviderHref={(p, s) => ({ pathname: 'todo' })}
    connectionHref={(connection, params, state) =>
      resolvers.create.start.connection.selectAction({
        connection,
        ...params,
        ...state,
      })
    }
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
    backHref={resolvers.create.start.selectStep}
    cancelHref={resolvers.list}
    header={<IntegrationCreatorBreadcrumbs step={1} />}
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
    header={<IntegrationCreatorBreadcrumbs step={1} subStep={2} />}
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
    postConfigureHref={(integration, params) =>
      resolvers.create.finish.selectStep({
        integration,
        ...params,
        position: '1',
      })
    }
  />
);

const finishStepSelectConnectionPage = (
  <SelectConnectionPage
    backHref={(p, s) => {
      const startStep = getSteps(s.integration, 0)[0];
      return resolvers.create.start.connection.configureAction({
        ...p,
        ...s,
        actionId: startStep.action!.id!,
        connection: startStep.connection!,
        integration: getEmptyIntegration(), // reset the integration object to force a re-add of the step, to avoid multiple steps being appended
      });
    }}
    cancelHref={resolvers.list}
    header={<IntegrationCreatorBreadcrumbs step={2} />}
    apiProviderHref={(p, s) => ({ pathname: 'todo' })}
    connectionHref={(connection, params, state) =>
      resolvers.create.finish.connection.selectAction({
        connection,
        ...params,
        ...state,
      })
    }
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
    backHref={(p, s) => resolvers.create.finish.selectStep({ ...p, ...s })}
    cancelHref={resolvers.list}
    header={<IntegrationCreatorBreadcrumbs step={2} />}
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
    header={<IntegrationCreatorBreadcrumbs step={2} subStep={2} />}
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
    header={<IntegrationCreatorBreadcrumbs step={3} />}
    apiProviderHref={(p, s) => ({ pathname: 'todo' })}
    connectionHref={(connection, p, s) =>
      resolvers.create.configure.addStep.connection.selectAction({
        connection,
        ...p,
        ...s,
      })
    }
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
    header={<IntegrationCreatorBreadcrumbs step={3} />}
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
    header={<IntegrationCreatorBreadcrumbs step={3} />}
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
    header={<IntegrationCreatorBreadcrumbs step={3} />}
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
    header={<IntegrationCreatorBreadcrumbs step={3} />}
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
        {/* step 1.1 */}
        <Route
          path={routes.create.start.selectStep}
          exact={true}
          children={startStepSelectConnectionPage}
        />
        {/* step 1.2 */}
        <Route
          path={routes.create.start.connection.selectAction}
          exact={true}
          children={startStepSelectActionPage}
        />
        {/* step 1.3 */}
        <Route
          path={routes.create.start.connection.configureAction}
          exact={true}
          children={startStepConfigureActionPage}
        />
        {/* step 2.1 */}
        <Route
          path={routes.create.finish.selectStep}
          exact={true}
          children={finishStepSelectConnectionPage}
        />
        {/* step 2.2 */}
        <Route
          path={routes.create.finish.connection.selectAction}
          exact={true}
          children={finishStepSelectActionPage}
        />
        {/* step 2.3 */}
        <Route
          path={routes.create.finish.connection.configureAction}
          exact={true}
          children={finishStepConfigureActionPage}
        />
        {/* step 3: index */}
        <Route
          path={routes.create.configure.index}
          exact={true}
          children={addStepPage}
        />
        {/* step 3: add connection.1 */}
        <Route
          path={routes.create.configure.addStep.selectStep}
          exact={true}
          children={addStepSelectConnectionPage}
        />
        {/* step 3: add connection.2 */}
        <Route
          path={routes.create.configure.addStep.connection.selectAction}
          exact={true}
          children={addStepSelectActionPage}
        />
        {/* step 3: add connection.3 */}
        <Route
          path={routes.create.configure.addStep.connection.configureAction}
          exact={true}
          children={addStepConfigureActionPage}
        />
        {/* step 3: edit connection.2 (this is optional and can be reached only from the configuration page), must be declared before the configure route */}
        <Route
          path={routes.create.configure.editStep.connection.selectAction}
          exact={true}
          children={editStepSelectActionPage}
        />
        {/* step 3: edit connection.1 (when editing we link directly to the configuration step) */}
        <Route
          path={routes.create.configure.editStep.connection.configureAction}
          exact={true}
          children={editStepConfigureActionPage}
        />
        {/* step 4 */}
        <Route
          path={routes.create.configure.saveAndPublish}
          exact={true}
          children={saveIntegrationPage}
        />
      </Switch>
    </WithClosedNavigation>
  );
};
