import * as React from 'react';
import { Route, Switch } from 'react-router';
import ApiConnectorsPage from './pages/ApiConnectorsPage';
import routes from './routes';

export class ApiClientConnectorsModule extends React.Component {
  public render() {
    return (
      <Switch>
        <Route path={routes.list} exact={true} component={ApiConnectorsPage} />
      </Switch>
    );
  }
}
