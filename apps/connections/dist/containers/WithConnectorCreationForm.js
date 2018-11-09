import * as React from 'react';
export function loadModule(connectorId) {
    switch (connectorId) {
        case 'ftp':
            return import('@syndesis/connector-ftp');
        default:
            return Promise.reject();
    }
}
export class WithConnectorCreationForm extends React.Component {
    constructor() {
        super(...arguments);
        this.state = {
            loading: true,
            error: false
        };
    }
    async componentDidMount() {
        try {
            this.setState({
                loading: true
            });
            const { CreationForm } = await loadModule(this.props.connectorId);
            this.setState({
                CreationForm,
                loading: false
            });
        }
        catch (e) {
            this.setState({
                error: true,
                loading: false
            });
        }
    }
    render() {
        return this.props.children(this.state);
    }
}
//# sourceMappingURL=WithConnectorCreationForm.js.map