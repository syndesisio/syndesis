import { ConnectionCard, ConnectionsGrid, ListViewToolbar } from "@syndesis/ui";
import { getConnectionIcon } from "@syndesis/utils";
import * as React from 'react';
import { Link } from 'react-router-dom';
export class ConnectionsListView extends React.Component {
    render() {
        return (React.createElement(React.Fragment, null,
            React.createElement(ListViewToolbar, Object.assign({}, this.props),
                React.createElement("div", { className: "form-group" },
                    React.createElement(Link, { to: `${this.props.baseurl}/create`, className: 'btn btn-primary' }, "Create Connection"))),
            React.createElement("div", { className: 'container-fluid' },
                React.createElement(ConnectionsGrid, { loading: this.props.loading }, this.props.connections.map((c, index) => (React.createElement(ConnectionCard, { name: c.name, description: c.description || '', icon: getConnectionIcon(c, process.env.PUBLIC_URL), key: index })))))));
    }
}
//# sourceMappingURL=ConnectionsListView.js.map