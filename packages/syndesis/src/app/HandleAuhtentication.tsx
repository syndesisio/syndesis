import * as React from 'react';
import { Redirect } from 'react-router-dom';

export class HandleAuhtentication extends React.Component {
  public componentWillMount() {
    // // eslint-disable-next-line
    // var storedNonce = StorageHelper.loadNonce();
    // var params = UrlHelper.getParams(window.location.href);
    // OidcValues.idToken = params['id_token'];
    // OidcValues.accessToken = params['access_token'];
    // this.redirectTo = decodeURIComponent(params['state']);
    // // Set authenticated to true
    // AuthStatus.isAuthenticated = true;
  }

  public render() {
    return <Redirect to={'/'} />;
  }
}
