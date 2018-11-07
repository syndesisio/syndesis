import * as React from "react";
import { Route, Switch } from "react-router";
import IntegrationsPage from "../pages/IntegrationsPage";

export interface IIntegrationsAppProps {
  mountpoint: string;
}

export class IntegrationsApp extends React.Component<IIntegrationsAppProps> {
  public render() {
    return (
      <Switch>
        <Route
          path={this.props.mountpoint}
          exact={true}
          component={IntegrationsPage}
        />
      </Switch>
    );
  }
}
