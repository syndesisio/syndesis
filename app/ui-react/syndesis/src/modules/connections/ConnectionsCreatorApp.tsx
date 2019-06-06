import * as React from 'react';
import { Route, Switch } from 'react-router';
import { WithClosedNavigation } from '../../shared';
import { ConfigurationPage } from './pages/create/ConfigurationPage';
import { ConnectorsPage } from './pages/create/ConnectorsPage';
import { ReviewPage } from './pages/create/ReviewPage';
import routes from './routes';

export default class ConnectionsCreatorApp extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <Switch>
          <Route
            path={routes.create.selectConnector}
            exact={true}
            component={ConnectorsPage}
          />
          <Route
            path={routes.create.configureConnector}
            exact={true}
            component={ConfigurationPage}
          />
          <Route
            path={routes.create.review}
            exact={true}
            component={ReviewPage}
          />
        </Switch>
      </WithClosedNavigation>
    );
  }
}
