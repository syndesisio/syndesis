import { DropdownKebab, Icon, ListView, MenuItem } from 'patternfly-react';
import * as React from 'react';
import { IntegrationStatus } from './IntegrationStatus';
import { IntegrationStatusDetail } from './IntegrationStatusDetail';
export class IntegrationsListItem extends React.Component {
    render() {
        return (React.createElement(ListView.Item, { actions: React.createElement("div", null,
                this.props.currentState === 'Pending' ? (React.createElement(IntegrationStatusDetail, { targetState: this.props.targetState, value: this.props.monitoringValue, currentStep: this.props.monitoringCurrentStep, totalSteps: this.props.monitoringTotalSteps })) : (React.createElement(IntegrationStatus, { currentState: this.props.currentState })),
                React.createElement(DropdownKebab, { id: `integration-${this.props.integrationId}-action-menu`, pullRight: true },
                    React.createElement(MenuItem, null, "Action 2"))), heading: this.props.integrationName, description: this.props.isConfigurationRequired ? (React.createElement(React.Fragment, null,
                React.createElement(Icon, { type: 'pf', name: 'warning-triangle-o' }),
                "Configuration Required")) : (''), hideCloseIcon: true, leftContent: React.createElement(ListView.Icon, { name: 'gear' }), stacked: false }));
    }
}
//# sourceMappingURL=IntegrationsListItem.js.map