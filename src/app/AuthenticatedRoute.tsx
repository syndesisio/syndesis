import * as React from 'react';
import { Redirect, Route, } from 'react-router-dom';
import { AppContext } from './AppContext';
import { AuthContext } from './AuthContext';

export class AuthenticatedRoute extends React.Component<any> {
  private Component: any;

  public constructor(props: any) {
    super(props);

    const {component: Component, ...componentProps} = props;
    this.Component = () => (
      <AppContext.Consumer>
        {({firstSetup}) => (
          <AuthContext.Consumer>
            {({logged}) => (
              logged
                ? <Component {...componentProps} />
                : <Redirect to={firstSetup ? '/settings' : '/login'}/>
            )}
          </AuthContext.Consumer>
        )}
      </AppContext.Consumer>
    );
  }

  public render() {
    const {component, ...props} = this.props;
    return (
      <Route {...props} children={this.Component}/>
    );
  }
}