import { ModuleLoader } from "@syndesis/ui";
import { WithRouter } from "@syndesis/utils";
import * as React from "react";
import Loadable from "react-loadable";

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
          <LoadableIntegrationsPage baseurl={match.url} />
        )}
      </WithRouter>
    );
  }
}
