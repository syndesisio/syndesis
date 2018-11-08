import { Spinner } from 'patternfly-react';
import * as React from 'react';
import { IntegrationProgress } from './IntegrationProgress';
export class IntegrationStatusDetail extends React.Component {
    render() {
        let fallbackText = 'Pending';
        switch (this.props.targetState) {
            case 'Published':
                fallbackText = 'Starting...';
                break;
            case 'Unpublished':
                fallbackText = 'Stopping...';
                break;
        }
        return (React.createElement("div", { className: 'integration-status-detail' }, this.props.value && this.props.currentStep && this.props.totalSteps ? (React.createElement(IntegrationProgress, { currentStep: this.props.currentStep, totalSteps: this.props.totalSteps, value: this.props.value })) : (React.createElement(React.Fragment, null,
            React.createElement(Spinner, { loading: true, inline: true }),
            fallbackText))));
    }
}
//# sourceMappingURL=IntegrationStatusDetail.js.map