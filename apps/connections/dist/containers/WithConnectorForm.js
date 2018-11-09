import * as React from 'react';
export class WithConnectorForm extends React.Component {
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
            const ConnectorForm = await import(`@syndesis/connector-${this.props.connectorId}`);
            console.log("???", ConnectorForm);
            this.setState({
                ConnectorForm: ConnectorForm,
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
//# sourceMappingURL=WithConnectorForm.js.map