import { AggregateStatusCount, Card } from 'patternfly-react';
import * as React from 'react';
export class ConnectionsMetric extends React.PureComponent {
    render() {
        return (React.createElement(Card, { accented: true, aggregated: true, matchHeight: true },
            React.createElement(Card.Title, null,
                React.createElement(AggregateStatusCount, null, this.props.count),
                ' ',
                "Connections")));
    }
}
//# sourceMappingURL=ConnectionsMetric.js.map