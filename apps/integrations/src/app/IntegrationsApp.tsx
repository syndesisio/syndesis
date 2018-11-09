import * as React from "react";
import { Route, Switch } from "react-router";
import IntegrationsPage from "../pages/IntegrationsPage";

export interface IIntegrationsAppProps {
  baseurl: string;
}

export class IntegrationsApp extends React.Component<IIntegrationsAppProps> {
  public render() {
    return (
      <Switch>
        <Route
          path={this.props.baseurl}
          exact={true}
          component={IntegrationsPage}
        />
      </Switch>
    );
  }
}
