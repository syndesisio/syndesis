import * as React from 'react';
import { Route, Switch } from 'react-router';
import {
  IntegrationCreatorFinishActionPage,
  IntegrationCreatorFinishConfigurationPage,
  IntegrationCreatorFinishConnectionPage,
  IntegrationCreatorSaveOrAddStepPage,
  IntegrationCreatorStartActionPage,
  IntegrationCreatorStartConfigurationPage,
  IntegrationCreatorStartConnectionPage,
  IntegrationsPage,
  TestAtlasmapPage,
} from './pages';
import routes from './routes';

export class IntegrationsModule extends React.Component {
  public render() {
    return (
      <Switch>
        <Route
          path={routes.integrations.create.start.configureAction}
          exact={true}
          component={IntegrationCreatorStartConfigurationPage}
        />
        <Route
          path={routes.integrations.create.start.selectAction}
          exact={true}
          component={IntegrationCreatorStartActionPage}
        />
        <Route
          path={routes.integrations.create.start.selectConnection}
          exact={true}
          component={IntegrationCreatorStartConnectionPage}
        />
        <Route
          path={routes.integrations.create.finish.configureAction}
          exact={true}
          component={IntegrationCreatorFinishConfigurationPage}
        />
        <Route
          path={routes.integrations.create.finish.selectAction}
          exact={true}
          component={IntegrationCreatorFinishActionPage}
        />
        <Route
          path={routes.integrations.create.finish.selectConnection}
          exact={true}
          component={IntegrationCreatorFinishConnectionPage}
        />
        <Route
          path={routes.integrations.create.configure.index}
          exact={true}
          component={IntegrationCreatorSaveOrAddStepPage}
        />
        <Route
          path={routes.integrations.list}
          exact={true}
          component={IntegrationsPage}
        />
        <Route
          path={'/integrations/atlasmap'}
          exact={true}
          component={TestAtlasmapPage}
        />
      </Switch>
    );
  }
}
