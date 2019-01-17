import * as React from 'react';
import { Route, Switch } from 'react-router';
import { create } from './pages';
import routes from './routes';

export class IntegrationCreatorApp extends React.Component {
  public render() {
    return (
      <Switch>
        <Route
          path={routes.create.start.configureAction}
          exact={true}
          component={create.start.StartConfigurationPage}
        />
        <Route
          path={routes.create.start.selectAction}
          exact={true}
          component={create.start.StartActionPage}
        />
        <Route
          path={routes.create.start.selectConnection}
          exact={true}
          component={create.start.StartConnectionPage}
        />
        <Route
          path={routes.create.finish.configureAction}
          exact={true}
          component={create.finish.FinishConfigurationPage}
        />
        <Route
          path={routes.create.finish.selectAction}
          exact={true}
          component={create.finish.FinishActionPage}
        />
        <Route
          path={routes.create.finish.selectConnection}
          exact={true}
          component={create.finish.FinishConnectionPage}
        />
        <Route
          path={routes.create.configure.index}
          exact={true}
          component={create.configure.addConnection.SaveOrAddStepPage}
        />
        <Route
          path={routes.create.configure.addConnection.selectConnection}
          exact={true}
          component={create.configure.addConnection.SelectConnectionPage}
        />
        <Route
          path={routes.create.configure.addConnection.selectAction}
          exact={true}
          component={create.configure.addConnection.SelectActionPage}
        />
        <Route
          path={routes.create.configure.addConnection.configureAction}
          exact={true}
          component={create.configure.addConnection.ConfigureActionPage}
        />
        <Route
          path={routes.create.configure.editConnection.selectAction}
          exact={true}
          component={create.configure.editConnection.SelectActionPage}
        />
        <Route
          path={routes.create.configure.editConnection.configureAction}
          exact={true}
          component={create.configure.editConnection.ConfigureActionPage}
        />
      </Switch>
    );
  }
}
