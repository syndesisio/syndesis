import { AggregateStatusCount, AggregateStatusNotification, AggregateStatusNotifications, Card, Icon } from "patternfly-react";
import * as React from "react";
export class AggregatedMetricCard extends React.PureComponent {
    render() {
        return (React.createElement(Card, { accented: true, aggregated: true, matchHeight: true },
            React.createElement(Card.Title, null,
                React.createElement(AggregateStatusCount, null,
                    React.createElement("span", { "data-test-aggregate-title": true }, this.props.title))),
            React.createElement(Card.Body, null,
                React.createElement(AggregateStatusNotifications, null,
                    React.createElement(AggregateStatusNotification, null,
                        React.createElement(Icon, { type: "pf", name: "ok" }),
                        React.createElement("span", { "data-test-aggregate-ok-count": true }, this.props.ok),
                        " "),
                    React.createElement(AggregateStatusNotification, null,
                        React.createElement(Icon, { type: "pf", name: "error-circle-o" }),
                        React.createElement("span", { "data-test-aggregate-error-count": true }, this.props.error))))));
    }
}
//# sourceMappingURL=AggregatedMetricCard.js.map