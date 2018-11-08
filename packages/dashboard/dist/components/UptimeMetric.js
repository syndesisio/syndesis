import { Card } from 'patternfly-react';
import * as React from 'react';
export class UptimeMetric extends React.PureComponent {
    render() {
        const startAsDate = new Date(this.props.start);
        const startAsHuman = startAsDate.toLocaleString();
        return (React.createElement(Card, { accented: true, aggregated: true, matchHeight: true },
            React.createElement(Card.Title, { className: 'text-left' },
                React.createElement("small", { className: 'pull-right' },
                    "since ",
                    startAsHuman),
                React.createElement("div", null, "Uptime")),
            React.createElement(Card.Body, null, "TODO")));
    }
}
//# sourceMappingURL=UptimeMetric.js.map