import * as React from "react";
import { Route, Switch } from "react-router";
import IntegrationsPage from "../pages/IntegrationsPage";
export class IntegrationsApp extends React.Component {
    render() {
        return (React.createElement(Switch, null,
            React.createElement(Route, { path: this.props.baseurl, exact: true, component: IntegrationsPage })));
    }
}
//# sourceMappingURL=IntegrationsApp.js.map