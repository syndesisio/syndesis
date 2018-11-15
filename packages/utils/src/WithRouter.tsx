import * as React from 'react';
import { RouteComponentProps, withRouter } from 'react-router';

export interface IWithRouterProps extends RouteComponentProps {
  children(router: RouteComponentProps): any;
}

export class WithRouterBase extends React.Component<IWithRouterProps> {
  public render() {
    const { children, ...props } = this.props;
    return children(props);
  }
}

export const WithRouter = withRouter<IWithRouterProps>(WithRouterBase);
