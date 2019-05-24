import * as React from 'react';
import { Route, Switch } from 'react-router';
import DashboardPage from './pages/DashboardPage';

export interface IDashboardAppProps {
  baseurl: string;
}

export default class DashboardApp extends React.Component<IDashboardAppProps> {
  public render() {
    return (
      <Switch>
        <Route
          path={this.props.baseurl}
          exact={true}
          component={DashboardPage}
        />
      </Switch>
    );
  }
}
