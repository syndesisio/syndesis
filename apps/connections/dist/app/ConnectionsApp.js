import * as React from "react";
import { Route, Switch } from "react-router";
import ConnectionsPage from "../pages/ConnectionsPage";
import NewConnectionPage from "../pages/NewConnectionPage";
import { ConnectionsAppContext } from "./ConnectionsAppContext";
export class ConnectionsApp extends React.Component {
    render() {
        return (React.createElement(ConnectionsAppContext.Provider, { value: this.props },
            React.createElement(Switch, null,
                React.createElement(Route, { path: this.props.baseurl, exact: true, component: ConnectionsPage }),
                React.createElement(Route, { path: `${this.props.baseurl}/new`, exact: true, component: NewConnectionPage }))));
    }
}
//# sourceMappingURL=ConnectionsApp.js.map