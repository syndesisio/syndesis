import { Card, EmptyState } from 'patternfly-react';
import * as React from 'react';
export class ConnectionCard extends React.PureComponent {
    render() {
        return (React.createElement(Card, { matchHeight: true },
            React.createElement(Card.Body, null,
                React.createElement(EmptyState, null,
                    React.createElement("div", { className: "blank-slate-pf-icon" },
                        React.createElement("img", { src: this.props.icon, alt: this.props.name, width: 46 })),
                    React.createElement(EmptyState.Title, null, this.props.name),
                    React.createElement(EmptyState.Info, null, this.props.description)))));
    }
}
//# sourceMappingURL=ConnectionCard.js.map