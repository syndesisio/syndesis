import * as React from 'react';
import { Route, Switch } from 'react-router';
import ConnectionsPage from './pages/ConnectionsPage';
import ConnectorFormPage from './pages/ConnectorFormPage';
import ConnectorsPage from './pages/ConnectorsPage';
import routes from './routes';

export default class ConnectionsApp extends React.Component {
  public render() {
    return (
      <Switch>
        <Route
          path={routes.connections}
          exact={true}
          component={ConnectionsPage}
        />
        <Route
          path={routes.create.selectConnector}
          exact={true}
          component={ConnectorsPage}
        />
        <Route
          path={routes.create.configureConnector}
          exact={true}
          component={ConnectorFormPage}
        />
      </Switch>
    );
  }
}
