import * as React from 'react';
import { SyndesisRest } from "./SyndesisRest";
import { WithIntegrations } from "./WithIntegrations";
export class WithMonitoredIntegrations extends React.Component {
    render() {
        return (React.createElement(WithIntegrations, null, ({ data: integrations, ...props }) => (React.createElement(SyndesisRest, { url: '/api/v1/monitoring/integrations', poll: 5000, defaultValue: [] }, ({ data: monitorings }) => {
            return this.props.children({
                ...props,
                data: {
                    items: integrations.items.map((i) => ({
                        integration: i,
                        overview: monitorings.find((o) => o.id === i.id)
                    })),
                    totalCount: integrations.totalCount
                }
            });
        }))));
    }
}
//# sourceMappingURL=WithMonitoredIntegrations.js.map