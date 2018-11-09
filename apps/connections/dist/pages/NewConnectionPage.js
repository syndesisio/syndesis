import { WithConnectors } from "@syndesis/api";
import { ConnectionCard, ConnectionsGrid } from "@syndesis/ui";
import { getConnectionIcon, WithRouter } from "@syndesis/utils";
import * as React from 'react';
import { Link } from "react-router-dom";
export default class NewConnectionPage extends React.Component {
    render() {
        return (React.createElement(WithRouter, null, ({ match }) => React.createElement(WithConnectors, null, ({ data, hasData, loading }) => React.createElement("div", { className: 'container-fluid' },
            React.createElement(ConnectionsGrid, { loading: loading }, data.items.map((c, index) => (React.createElement(Link, { to: `${match.url}/${c.id}`, style: { color: 'inherit', textDecoration: 'none' }, key: index },
                React.createElement(ConnectionCard, { name: c.name, description: c.description || '', icon: getConnectionIcon(c, process.env.PUBLIC_URL) })))))))));
    }
}
//# sourceMappingURL=NewConnectionPage.js.map