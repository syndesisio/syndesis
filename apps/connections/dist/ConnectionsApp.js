import * as React from "react";
import { Route, Switch } from "react-router";
import ConnectionsPage from "./pages/ConnectionsPage";
import ConnectorFormPage from "./pages/ConnectorFormPage";
import ConnectorsPage from "./pages/ConnectorsPage";
import { ConnectionsAppContext } from "./ConnectionsAppContext";
export class ConnectionsApp extends React.Component {
    render() {
        return (React.createElement(ConnectionsAppContext.Provider, { value: this.props },
            React.createElement(Switch, null,
                React.createElement(Route, { path: this.props.baseurl, exact: true, component: ConnectionsPage }),
                React.createElement(Route, { path: `${this.props.baseurl}/create`, exact: true, component: ConnectorsPage }),
                React.createElement(Route, { path: `${this.props.baseurl}/create/:connectorId`, exact: true, component: ConnectorFormPage }))));
    }
}
//# sourceMappingURL=ConnectionsApp.js.map