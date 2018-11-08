import { ProgressBar } from 'patternfly-react';
import * as React from 'react';
export class IntegrationProgress extends React.PureComponent {
    render() {
        return (React.createElement("div", null,
            React.createElement("div", null,
                this.props.value,
                " ( ",
                this.props.currentStep,
                " /",
                ' ',
                this.props.totalSteps,
                " )"),
            React.createElement(ProgressBar, { now: this.props.currentStep, max: this.props.totalSteps, style: {
                    height: 6,
                } })));
    }
}
//# sourceMappingURL=IntegrationProgress.js.map