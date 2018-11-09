import { IntegrationsList, IntegrationsListItem, IntegrationsListSkeleton, ListViewToolbar } from "@syndesis/ui";
import * as React from 'react';
import { Link } from 'react-router-dom';
export class IntegrationsListView extends React.Component {
    render() {
        return (React.createElement(React.Fragment, null,
            React.createElement(ListViewToolbar, Object.assign({}, this.props),
                React.createElement("div", { className: "form-group" },
                    React.createElement(Link, { to: `${this.props.match}/import`, className: 'btn btn-default' }, "Import"),
                    React.createElement(Link, { to: `${this.props.match}/new`, className: 'btn btn-primary' }, "Create Integration"))),
            React.createElement("div", { className: 'container-fluid' }, this.props.loading ? (React.createElement(IntegrationsListSkeleton, { width: 800, style: {
                    backgroundColor: '#FFF',
                    marginTop: 30
                } })) : (React.createElement(IntegrationsList, null, this.props.monitoredIntegrations.map((mi, index) => (React.createElement(IntegrationsListItem, { integrationId: mi.integration.id, integrationName: mi.integration.name, currentState: mi.integration.currentState, targetState: mi.integration.targetState, isConfigurationRequired: !!(mi.integration.board.warnings ||
                    mi.integration.board.errors ||
                    mi.integration.board.notices), monitoringValue: mi.monitoring && mi.monitoring.detailedState.value, monitoringCurrentStep: mi.monitoring && mi.monitoring.detailedState.currentStep, monitoringTotalSteps: mi.monitoring && mi.monitoring.detailedState.totalSteps, key: index }))))))));
    }
}
//# sourceMappingURL=IntegrationsListView.js.map