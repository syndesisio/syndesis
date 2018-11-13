import * as React from 'react';
import { SyndesisRest } from "./SyndesisRest";
export class WithIntegrationsMetrics extends React.Component {
    render() {
        return (React.createElement(SyndesisRest, { url: '/api/v1/metrics/integrations', poll: 5000, defaultValue: {
                start: `${Date.now()}`,
                errors: 0,
                messages: 0,
                lastProcessed: `${Date.now()}`,
                metricsProvider: 'null',
                integrationDeploymentMetrics: [],
                topIntegrations: {},
                id: '-1',
            } }, response => this.props.children(response)));
    }
}
//# sourceMappingURL=WithIntegrationsMetrics.js.map