import { Spinner } from 'patternfly-react';
import * as React from 'react';
// import './ModuleLoader.css';
import { UnrecoverableError } from './UnrecoverableError';
export class ModuleLoader extends React.Component {
    render() {
        if (this.props.error || this.props.timedOut) {
            console.error(this.props.error); // tslint:disable-line
            return React.createElement(UnrecoverableError, null);
        }
        else if (this.props.pastDelay) {
            return (React.createElement("div", { className: 'ModuleLoader' },
                React.createElement(Spinner, { loading: true, size: 'lg' })));
        }
        return null;
    }
}
//# sourceMappingURL=ModuleLoader.js.map