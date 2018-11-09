import { WithRouter } from "@syndesis/utils";
import { Spinner } from 'patternfly-react';
import * as React from 'react';
import { WithConnectorCreationForm } from "../containers";
export default class ConnectorFormPage extends React.Component {
    render() {
        return (React.createElement(WithRouter, null, ({ match }) => React.createElement(WithConnectorCreationForm, { connectorId: match.params.connectorId }, ({ CreationForm, loading, error }) => React.createElement("div", { className: 'container-fluid' }, (loading || error)
            ? (loading
                ? React.createElement(Spinner, { loading: true, size: 'lg' })
                : React.createElement("p", null, "Connector not found. Perhaps we could build a form from the json?")) : React.createElement(CreationForm, null)))));
    }
}
//# sourceMappingURL=ConnectorFormPage.js.map