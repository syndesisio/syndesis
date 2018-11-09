import { Card, DonutChart, patternfly } from "patternfly-react";
import * as React from "react";
export class IntegrationBoard extends React.PureComponent {
    render() {
        const data = {
            colors: {
                Pending: patternfly.pfPaletteColors.black200,
                Published: patternfly.pfPaletteColors.blue400,
                Stopped: patternfly.pfPaletteColors.black300
            },
            columns: [
                ["Running", this.props.runningIntegrations],
                ["Stopped", this.props.stoppedIntegrations],
                ["Pending", this.props.pendingIntegrations]
            ],
            type: "donut"
        };
        return (React.createElement(Card, null,
            React.createElement(Card.Heading, null,
                React.createElement(Card.Title, null, "Integration Board")),
            React.createElement(Card.Body, null,
                React.createElement(DonutChart, { id: "integration-board", size: { height: 120 }, data: data, tooltip: {
                        contents: patternfly.pfDonutTooltipContents,
                        show: true
                    }, title: { type: "total", secondary: "Integrations" }, legend: { show: true, position: "right" } }))));
    }
}
//# sourceMappingURL=IntegrationBoard.js.map