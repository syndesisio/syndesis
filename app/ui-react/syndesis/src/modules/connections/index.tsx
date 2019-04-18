import * as React from 'react';
import { Route, Switch } from 'react-router';
import ConnectionsCreatorApp from './ConnectionsCreatorApp';
import { ConnectionDetailsPage, ConnectionsPage } from './pages';
import routes from './routes';

export class ConnectionsModule extends React.Component {
  public render() {
    return (
      <Switch>
        <Route path={routes.create.root} component={ConnectionsCreatorApp} />
        <Route path={routes.connections} exact={true} component={ConnectionsPage} />
        <Route
          path={routes.connection.details}
          exact={true}
          component={ConnectionDetailsPage}
        />
      </Switch>
    );
  }
}
