import { ModuleLoader } from "@syndesis/ui";
import { WithRouter } from "@syndesis/utils";
import * as React from "react";
import Loadable from 'react-loadable';

// this will be part of the main bundle, which sux but I have no idea how
// to avoid it right now
// import "@syndesis/connections/dist/connections.css";

const LoadableConnectionsPage = Loadable({
  loader: () =>
    import(/* webpackChunkName: "Connections" */  "@syndesis/connections"),
  loading: ModuleLoader
});

export class ConnectionsModule extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => (
          <LoadableConnectionsPage baseurl={match.url}/>
        )}
      </WithRouter>
    );
  }
}
