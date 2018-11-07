import * as React from "react";
import { Route, Switch } from "react-router";
import ConnectionsPage from "../pages/ConnectionsPage";
import NewConnectionPage from "../pages/NewConnectionPage";

export interface IConnectionsAppProps {
  mountpoint: string;
}

export class ConnectionsApp extends React.Component<IConnectionsAppProps> {
  public render() {
    return (
      <Switch>
        <Route
          path={this.props.mountpoint}
          exact={true}
          component={ConnectionsPage}
        />
        <Route
          path={`${this.props.mountpoint}/new`}
          exact={true}
          component={NewConnectionPage}
        />
      </Switch>
    );
  }
}
