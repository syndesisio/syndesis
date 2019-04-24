import { getConnectionIcon } from '@syndesis/api';
import { Integration } from '@syndesis/models';
import { Breadcrumb } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Route, Switch } from 'react-router';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../shared';
import {
  IntegrationEditorBreadcrumbs,
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
      resolvers.integration.edit.addStep.selectStep({
        position: `${position}`,
        ...p,
        ...s,
      })
    }
    getEditConfigureStepHrefCallback={(stepIdx, step, p, s) =>
      resolvers.integration.edit.editStep.configureAction({
        actionId: step.action!.id!,
        connection: step.connection!,
        position: `${stepIdx}`,
        ...p,
        ...s,
      })
    }
    header={<IntegrationEditorBreadcrumbs step={1} />}
    nextHref={(p, s) =>
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
    header={<IntegrationEditorBreadcrumbs step={1} />}
    selectHref={(connection, p, s) =>
      resolvers.integration.edit.addStep.stepSwitcher({
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

const saveIntegrationPage = (
  <SaveIntegrationPage
    backHref={(p, s) => resolvers.integration.edit.index({ ...p, ...s })}
    cancelHref={resolvers.list}
    header={<IntegrationEditorBreadcrumbs step={2} />}
    postSaveHref={resolvers.list}
  />
);

const addStepSelectActionPage = (
  <SelectActionPage
    cancelHref={(p, s) => resolvers.integration.edit.index({ ...p, ...s })}
    header={<IntegrationEditorBreadcrumbs step={1} />}
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
      resolvers.integration.edit.addStep.configureAction({
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
      resolvers.integration.edit.addStep.selectAction({ ...p, ...s })
    }
    cancelHref={(p, s) => resolvers.integration.edit.index({ ...p, ...s })}
    header={<IntegrationEditorBreadcrumbs step={1} />}
    mode={'adding'}
    nextStepHref={(p, s) =>
      resolvers.integration.edit.addStep.configureAction({
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
    postConfigureHref={(integration, flow) =>
      resolvers.integration.edit.index({
        flow,
        integration,
      })
    }
  />
);

const editStepSelectActionPage = (
  <SelectActionPage
    cancelHref={(p, s) => resolvers.integration.edit.index({ ...p, ...s })}
    header={<IntegrationEditorBreadcrumbs step={1} />}
    sidebar={({ steps, activeIndex }) => (
      <IntegrationEditorSidebar steps={steps} activeIndex={activeIndex} />
    )}
    selectHref={(actionId, p, s) =>
      resolvers.integration.edit.editStep.configureAction({
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
      resolvers.integration.edit.editStep.selectAction({ ...p, ...s })
    }
    cancelHref={(p, s) => resolvers.integration.edit.index({ ...p, ...s })}
    header={<IntegrationEditorBreadcrumbs step={1} />}
    mode={'editing'}
    nextStepHref={(p, s) =>
      resolvers.integration.edit.editStep.configureAction({
        ...p,
        ...s,
      })
    }
    sidebar={({ steps, activeIndex }) => (
      <IntegrationEditorSidebar steps={steps} activeIndex={activeIndex} />
    )}
    postConfigureHref={(integration, flow) =>
      resolvers.integration.edit.index({
        flow,
        integration,
      })
    }
  />
);

export interface IIntegrationEditorAppRouteState {
  integration: Integration;
}

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
            <Link to={resolvers.integration.details({ integration })}>
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
            <Route
              path={routes.integration.edit.addStep.selectStep}
              exact={true}
              children={selectConnectionPage}
            />
            <Route
              path={routes.integration.edit.addStep.connection.selectAction}
              exact={true}
              children={addStepSelectActionPage}
            />
            <Route
              path={routes.integration.edit.addStep.connection.configureAction}
              exact={true}
              children={addStepConfigureActionPage}
            />
            <Route
              path={routes.integration.edit.editStep.connection.selectAction}
              exact={true}
              children={editStepSelectActionPage}
            />
            <Route
              path={routes.integration.edit.editStep.connection.configureAction}
              exact={true}
              children={editStepConfigureActionPage}
            />
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
