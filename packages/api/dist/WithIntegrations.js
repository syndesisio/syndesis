import * as React from 'react';
import { SyndesisRest } from "./SyndesisRest";
export class WithIntegrations extends React.Component {
    render() {
        return (React.createElement(SyndesisRest, { url: '/api/v1/integrations', poll: 5000, defaultValue: {
                items: [],
                totalCount: 0
            } }, response => this.props.children(response)));
    }
}
//# sourceMappingURL=WithIntegrations.js.map