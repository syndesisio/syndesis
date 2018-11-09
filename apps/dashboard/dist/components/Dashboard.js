import { AggregatedMetricCard, ConnectionCard, ConnectionsGrid } from "@syndesis/ui";
import { getConnectionIcon } from "@syndesis/utils";
import { CardGrid, Grid } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
// import './Dashboard.css';
import { ConnectionsMetric } from "./ConnectionsMetric";
import { IntegrationBoard } from "./IntegrationBoard";
import { RecentUpdates } from "./RecentUpdates";
import { TopIntegrations } from "./TopIntegrations";
import { UptimeMetric } from "./UptimeMetric";
export class Dashboard extends React.Component {
    render() {
        return (React.createElement("div", { className: 'container-fluid' },
            React.createElement(Grid, { fluid: true },
                React.createElement(Grid.Row, null,
                    React.createElement(Grid.Col, { sm: 12 },
                        React.createElement("div", { className: 'Dashboard-header' },
                            React.createElement("h1", { className: 'Dashboard-header__title' }, "System metric"),
                            React.createElement("div", { className: "Dashboard-header__actions" },
                                React.createElement(Link, { to: '/integrations' }, "View All Integrations"),
                                React.createElement(Link, { to: '/integrations/new', className: 'btn btn-primary' }, "Create Integration")))))),
            React.createElement(CardGrid, { fluid: true, matchHeight: true },
                React.createElement(CardGrid.Row, null,
                    React.createElement(CardGrid.Col, { sm: 6, md: 3 },
                        React.createElement(AggregatedMetricCard, { title: `${this.props.integrationsCount} Integrations`, ok: this.props.integrationsCount -
                                this.props.integrationsErrorCount, error: this.props.integrationsErrorCount })),
                    React.createElement(CardGrid.Col, { sm: 6, md: 3 },
                        React.createElement(ConnectionsMetric, { count: this.props.connectionsCount })),
                    React.createElement(CardGrid.Col, { sm: 6, md: 3 },
                        React.createElement(AggregatedMetricCard, { title: `${this.props.metrics.messages} Total Messages`, ok: this.props.metrics.messages - this.props.metrics.errors, error: this.props.metrics.errors })),
                    React.createElement(CardGrid.Col, { sm: 6, md: 3 },
                        React.createElement(UptimeMetric, { start: this.props.metrics.start })))),
            React.createElement(Grid, { fluid: true },
                React.createElement(Grid.Row, null,
                    React.createElement(Grid.Col, { sm: 12, md: 6 },
                        React.createElement(TopIntegrations, { loading: !this.props.integrationsLoaded, topIntegrations: this.props.topIntegrations })),
                    React.createElement(Grid.Col, { sm: 12, md: 6 },
                        React.createElement(Grid.Row, null,
                            React.createElement(Grid.Col, { sm: 12 },
                                React.createElement(IntegrationBoard, { runningIntegrations: this.props.runningIntegrations, pendingIntegrations: this.props.pendingIntegrations, stoppedIntegrations: this.props.stoppedIntegrations }))),
                        React.createElement(Grid.Row, null,
                            React.createElement(Grid.Col, { sm: 12 },
                                React.createElement(RecentUpdates, { loading: !this.props.integrationsLoaded, recentlyUpdatedIntegrations: this.props.recentlyUpdatedIntegrations })))))),
            React.createElement(Grid, { fluid: true, style: { marginTop: '20px' } },
                React.createElement(Grid.Row, null,
                    React.createElement(Grid.Col, { sm: 12 },
                        React.createElement("div", { className: 'Dashboard-header' },
                            React.createElement("h1", { className: 'Dashboard-header__title' }, "Connections"),
                            React.createElement("div", { className: "Dashboard-header__actions" },
                                React.createElement(Link, { to: '/connections' }, "View All Connections"),
                                React.createElement(Link, { to: '/connections/new', className: 'btn btn-primary' }, "Create Connection")))))),
            React.createElement(ConnectionsGrid, { loading: !this.props.connectionsLoaded }, this.props.connections.map((c, index) => (React.createElement(ConnectionCard, { name: c.name, description: c.description || '', icon: getConnectionIcon(c, process.env.PUBLIC_URL), key: index }))))));
    }
}
//# sourceMappingURL=Dashboard.js.map