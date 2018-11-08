import { IntegrationStatus } from '@syndesis/ui';
import { Card, Grid } from 'patternfly-react';
import * as React from 'react';
import { RecentUpdatesSkeleton } from './RecentUpdatesSkeleton';
export class RecentUpdates extends React.Component {
    render() {
        return (React.createElement(Card, { accented: false },
            React.createElement(Card.Heading, null,
                React.createElement(Card.Title, null, "Recent Updates")),
            React.createElement(Card.Body, null, this.props.loading ? (React.createElement(RecentUpdatesSkeleton, null)) : (React.createElement(Grid, { fluid: true }, this.props.recentlyUpdatedIntegrations.map(i => (React.createElement(Grid.Row, { key: i.id },
                React.createElement(Grid.Col, { sm: 5 }, i.name),
                React.createElement(Grid.Col, { sm: 3 },
                    React.createElement(IntegrationStatus, { currentState: i.currentState })),
                React.createElement(Grid.Col, { sm: 4 }, new Date(i.updatedAt || i.createdAt).toLocaleString())))))))));
    }
}
//# sourceMappingURL=RecentUpdates.js.map