import { ModuleLoader } from "@syndesis/ui";
import * as React from "react";
import Loadable from "react-loadable";
import { Route, Switch } from "react-router";
import { WithRouter } from "src/containers/index";

const LoadableDashboardPage = Loadable({
  loader: () =>
    import(/* webpackChunkName: "DashboardPageChunk" */ "./pages/DashboardPage"),
  loading: ModuleLoader
});

export class DashboardModule extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => (
          <Switch>
            <Route
              path={match.url}
              exact={true}
              component={LoadableDashboardPage}
            />
          </Switch>
        )}
      </WithRouter>
    );
  }
}
