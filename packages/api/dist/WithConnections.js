import * as React from 'react';
import { SyndesisRest } from "./SyndesisRest";
export class WithConnections extends React.Component {
    render() {
        return (React.createElement(SyndesisRest, { url: '/api/v1/connections', poll: 5000, defaultValue: {
                items: [],
                totalCount: 0
            } }, response => this.props.children(response)));
    }
}
//# sourceMappingURL=WithConnections.js.map