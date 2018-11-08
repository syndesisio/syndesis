import * as React from 'react';
import { SyndesisRest } from "./SyndesisRest";
export class WithIntegrationsMetrics extends React.Component {
    render() {
        return (React.createElement(SyndesisRest, { url: '/api/v1/metrics/integrations', poll: 5000, defaultValue: {
                errors: 0,
                lastProcessed: 0,
                messages: 0,
                metricsProvider: '',
                start: 0,
                topIntegrations: {}
            } }, response => this.props.children(response)));
    }
}
//# sourceMappingURL=WithIntegrationsMetrics.js.map