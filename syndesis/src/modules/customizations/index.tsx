import * as React from 'react';
import { Route, Switch } from 'react-router';
import CustomizationsApp from './CustomizationsApp';
import routes from './routes';

export class CustomizationsModule extends React.Component {
  public render() {
    return (
      <Switch>
        <Route path={routes.root} component={CustomizationsApp} />
      </Switch>
    );
  }
}
