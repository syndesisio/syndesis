import { CardGrid } from 'patternfly-react';
import * as React from 'react';
import { ConnectionSkeleton } from './ConnectionSkeleton';
export class ConnectionsGrid extends React.Component {
    render() {
        return (React.createElement(CardGrid, { fluid: true, matchHeight: true },
            React.createElement(CardGrid.Row, null, this.props.loading
                ? new Array(5).fill(0).map((_, index) => (React.createElement(CardGrid.Col, { sm: 6, md: 3, key: index },
                    React.createElement(ConnectionSkeleton, { key: index }))))
                : this.props.children.map((c, index) => (React.createElement(CardGrid.Col, { sm: 6, md: 3, key: index }, c))))));
    }
}
//# sourceMappingURL=ConnectionsGrid.js.map