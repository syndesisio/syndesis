import { Label } from 'patternfly-react';
import * as React from 'react';
export class IntegrationStatus extends React.Component {
    render() {
        const labelType = this.props.currentState === 'Published' ||
            this.props.currentState === 'Pending'
            ? 'primary'
            : 'default';
        let label = 'Pending';
        switch (this.props.currentState) {
            case 'Published':
                label = 'Published';
                break;
            case 'Unpublished':
                label = 'Unpublished';
                break;
        }
        return React.createElement(Label, { type: labelType }, label);
    }
}
//# sourceMappingURL=IntegrationStatus.js.map