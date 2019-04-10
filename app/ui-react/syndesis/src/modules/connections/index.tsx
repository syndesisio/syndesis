import * as React from 'react';
import { Route, Switch } from 'react-router';
import ConnectionsApp from './ConnectionsApp';
import routes from './routes';

export class ConnectionsModule extends React.Component {
  public render() {
    return (
      <Switch>
        <Route path={routes.root} component={ConnectionsApp} />
      </Switch>
    );
  }
}
