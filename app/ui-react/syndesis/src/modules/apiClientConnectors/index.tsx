import * as React from 'react';
import { Route, Switch } from 'react-router';
import ApiConnectorCreatorApp from './ApiConnectorCreatorApp';
import ApiConnectorDetailsPage from './pages/ApiConnectorDetailsPage';
import ApiConnectorsPage from './pages/ApiConnectorsPage';
import routes from './routes';

export class ApiClientConnectorsModule extends React.Component {
  public render() {
    return (
      <Switch>
        <Route path={routes.list} exact={true} component={ApiConnectorsPage} />
        <Route
          path={routes.apiConnector.edit}
          exact={true}
          children={<ApiConnectorDetailsPage edit={true} />}
        />
        <Route
          path={routes.apiConnector.details}
          exact={true}
          children={<ApiConnectorDetailsPage edit={false} />}
        />
        <Route path={routes.create.root} component={ApiConnectorCreatorApp} />
      </Switch>
    );
  }
}
