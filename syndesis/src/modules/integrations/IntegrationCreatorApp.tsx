import * as React from 'react';
import { Route, Switch } from 'react-router';
import {
  IntegrationCreatorFinishActionPage,
  IntegrationCreatorFinishConfigurationPage,
  IntegrationCreatorFinishConnectionPage,
  IntegrationCreatorSaveOrAddStepPage,
  IntegrationCreatorSelectActionPage,
  IntegrationCreatorSelectConnectionPage,
  IntegrationCreatorStartActionPage,
  IntegrationCreatorStartConfigurationPage,
  IntegrationCreatorStartConnectionPage,
} from './pages';
import routes from './routes';

export class IntegrationCreatorApp extends React.Component {
  public render() {
    return (
      <Switch>
        <Route
          path={routes.create.start.configureAction}
          exact={true}
          component={IntegrationCreatorStartConfigurationPage}
        />
        <Route
          path={routes.create.start.selectAction}
          exact={true}
          component={IntegrationCreatorStartActionPage}
        />
        <Route
          path={routes.create.start.selectConnection}
          exact={true}
          component={IntegrationCreatorStartConnectionPage}
        />
        <Route
          path={routes.create.finish.configureAction}
          exact={true}
          component={IntegrationCreatorFinishConfigurationPage}
        />
        <Route
          path={routes.create.finish.selectAction}
          exact={true}
          component={IntegrationCreatorFinishActionPage}
        />
        <Route
          path={routes.create.finish.selectConnection}
          exact={true}
          component={IntegrationCreatorFinishConnectionPage}
        />
        <Route
          path={routes.create.configure.index}
          exact={true}
          component={IntegrationCreatorSaveOrAddStepPage}
        />
        <Route
          path={routes.create.configure.addConnection.selectConnection}
          exact={true}
          component={IntegrationCreatorSelectConnectionPage}
        />
        <Route
          path={routes.create.configure.addConnection.selectAction}
          exact={true}
          component={IntegrationCreatorSelectActionPage}
        />
      </Switch>
    );
  }
}
