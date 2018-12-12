import * as React from 'react';
import { Route, Switch } from 'react-router';
import CustomizationsPage from './pages/CustomizationsPage';

export interface ICustomizationsAppProps {
  baseurl: string;
}

export default class CustomizationApp extends React.Component<
  ICustomizationsAppProps
> {
  public render() {
    return (
      <Switch>
        <Route
          path={this.props.baseurl}
          exact={true}
          component={CustomizationsPage}
        />
      </Switch>
    );
  }
}
