import * as React from 'react';
import { AuthContext, IAuthContext } from './AuthContext';

class LoginBase extends React.Component<IAuthContext> {
  public componentWillMount() {
    const authUrl = `${this.props.authorizationUri}?client_id=${
      this.props.clientId
    }&response_type=${
      this.props.responseType
    }&redirect_uri=${encodeURIComponent(this.props.redirectUri)}`;
    window.location.href = authUrl;
  }

  public render() {
    return <p>Redirect to auth...</p>;
  }
}

export const LoginPage = () => (
  <AuthContext.Consumer>
    {props => <LoginBase {...props} />}
  </AuthContext.Consumer>
);
