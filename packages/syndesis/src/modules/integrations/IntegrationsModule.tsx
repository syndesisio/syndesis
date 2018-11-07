import { ModuleLoader } from "@syndesis/ui";
import * as React from "react";
import Loadable from "react-loadable";
import { Route, Switch } from "react-router";
import { WithRouter } from "src/containers/index";

// this will be part of the main bundle, which sux but I have no idea how
// to avoid it right now
// import "@syndesis/integrations/dist/integrations.css";

const LoadableIntegrationsPage = Loadable({
  loader: () =>
    import(/* webpackChunkName: "Integrations" */  "@syndesis/integrations"),
  loading: ModuleLoader
});

export class IntegrationsModule extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => (
          <Switch>
            <Route
              path={match.url}
              exact={true}
              component={LoadableIntegrationsPage}
            />
          </Switch>
        )}
      </WithRouter>
    );
  }
}
