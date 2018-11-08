import { IntegrationsList, IntegrationsListItem, IntegrationsListSkeleton } from '@syndesis/ui';
import { Card, MenuItem } from 'patternfly-react';
import * as React from 'react';
export class TopIntegrations extends React.Component {
    render() {
        return (React.createElement(Card, { accented: false, className: 'TopIntegrations' },
            React.createElement(Card.Heading, null,
                React.createElement(Card.DropdownButton, { id: "cardDropdownButton1", title: "Last 30 Days" },
                    React.createElement(MenuItem, { eventKey: "1", active: true }, "Last 30 Days"),
                    React.createElement(MenuItem, { eventKey: "2" }, "Last 60 Days"),
                    React.createElement(MenuItem, { eventKey: "3" }, "Last 90 Days")),
                React.createElement(Card.Title, null, "Top 5 Integrations")),
            React.createElement(Card.Body, null, this.props.loading ? (React.createElement(IntegrationsListSkeleton, { width: 500 })) : (React.createElement(IntegrationsList, null, this.props.topIntegrations.map((mi, index) => (React.createElement(IntegrationsListItem, { integrationId: mi.integration.id, integrationName: mi.integration.name, currentState: mi.integration.currentState, targetState: mi.integration.targetState, isConfigurationRequired: !!(mi.integration.board.warnings ||
                    mi.integration.board.errors ||
                    mi.integration.board.notices), monitoringValue: mi.monitoring && mi.monitoring.detailedState.value, monitoringCurrentStep: mi.monitoring && mi.monitoring.detailedState.currentStep, monitoringTotalSteps: mi.monitoring && mi.monitoring.detailedState.totalSteps, key: index }))))))));
    }
}
//# sourceMappingURL=TopIntegrations.js.map