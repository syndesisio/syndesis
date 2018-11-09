import * as React from "react";
import { Route, Switch } from "react-router";
import ConnectionsPage from "./pages/ConnectionsPage";
import ConnectorFormPage from "./pages/ConnectorFormPage";
import ConnectorsPage from "./pages/ConnectorsPage";
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
            path={`${this.props.baseurl}/create`}
            exact={true}
            component={ConnectorsPage}
          />
          <Route
            path={`${this.props.baseurl}/create/:connectorId`}
            exact={true}
            component={ConnectorFormPage}
          />
        </Switch>
      </ConnectionsAppContext.Provider>
    );
  }
}
