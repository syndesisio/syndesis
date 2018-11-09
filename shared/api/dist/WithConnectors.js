import * as React from 'react';
import { SyndesisRest } from "./SyndesisRest";
export class WithConnectors extends React.Component {
    render() {
        return (React.createElement(SyndesisRest, { url: '/api/v1/connectors', defaultValue: {
                items: [],
                totalCount: 0
            } }, response => this.props.children(response)));
    }
}
//# sourceMappingURL=WithConnectors.js.map