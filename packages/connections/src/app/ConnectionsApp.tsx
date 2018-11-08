import * as React from "react";
import { Route, Switch } from "react-router";
import ConnectionsPage from "../pages/ConnectionsPage";
import NewConnectionPage from "../pages/NewConnectionPage";
import { ConnectionsAppContext } from "./ConnectionsAppContext";

export interface IConnectionsAppProps {
  baseurl: string;
}

export class ConnectionsApp extends React.Component<IConnectionsAppProps> {
  public render() {
    return (
      <ConnectionsAppContext.Provider value={this.props}>
        <Switch>
          <Route
            path={this.props.baseurl}
            exact={true}
            component={ConnectionsPage}
          />
          <Route
            path={`${this.props.baseurl}/new`}
            exact={true}
            component={NewConnectionPage}
          />
        </Switch>
      </ConnectionsAppContext.Provider>
    );
  }
}
