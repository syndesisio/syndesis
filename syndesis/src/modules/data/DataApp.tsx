import * as React from 'react';
import { Route, Switch } from 'react-router';
import VirtualizationsPage from './pages/VirtualizationsPage';

export interface IDataAppProps {
  baseurl: string;
}

export default class DataApp extends React.Component<IDataAppProps> {
  public render() {
    return (
      <Switch>
        <Route
          path={this.props.baseurl}
          exact={true}
          component={VirtualizationsPage}
        />
      </Switch>
    );
  }
}
