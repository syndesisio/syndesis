import * as React from 'react';
import { withRouter } from 'react-router';
export class WithRouterBase extends React.Component {
    render() {
        const { children, ...props } = this.props;
        return children(props);
    }
}
export const WithRouter = withRouter(WithRouterBase);
//# sourceMappingURL=WithRouter.js.map