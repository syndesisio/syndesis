import * as React from "react";
import { Route, Switch } from "react-router";
import DashboardPage from "../pages/DashboardPage";
export class DashboardApp extends React.Component {
    render() {
        return (React.createElement(Switch, null,
            React.createElement(Route, { path: this.props.baseurl, exact: true, component: DashboardPage })));
    }
}
//# sourceMappingURL=DashboardApp.js.map