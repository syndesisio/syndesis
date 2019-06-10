import { useConnectorCredentialsConnect } from '@syndesis/api';
import * as React from 'react';
import { UIContext } from '../../app';

export function useOAuthFlow(
  connectorId: string,
  connectorName: string,
  onSuccess: () => void
) {
  const { pushNotification } = React.useContext(UIContext);

  /**
   * retrieve the credentials cookie and redirectUrl for the connector OAuth flow.
   * If the connector requires OAuth, the resource will contain the data we need to
   * setup the flow.
   *
   * We set up the oauth flow so that the last page to be open will be the oauth-redirect.html
   * "asset", that is configured to call the authCompleted callback defined down here.
   */
  const {
    loading: isConnecting,
    resource: connectResource,
  } = useConnectorCredentialsConnect(
    connectorId,
    `${process.env.PUBLIC_URL}/oauth-redirect.html`
  );

  /**
   * create a callback reference to a function that will be globally available and
   * that will be used by the oauth2 popup landing page to pass back to this the
   * result of the connection
   */
  const authCompleted = React.useCallback(
    (authState: string) => {
      try {
        const auth = JSON.parse(decodeURIComponent(authState));
        if (auth.status === 'FAILURE') {
          throw new Error(auth.message);
        }
        pushNotification(auth.message, 'success');
        onSuccess();
      } catch (e) {
        pushNotification(`Connection failed: ${e.message}`, 'error');
      }
    },
    [pushNotification, onSuccess]
  );

  /**
   * make the above callback available under window.authCompleted, and remove it
   * when the page is unmounted
   */
  React.useEffect(() => {
    (window as any).authCompleted = authCompleted;

    return () => {
      delete (window as any).authCompleted;
    };
  }, [authCompleted]);

  /**
   * we need to clean up any existing cookie we might have set in previous visit
   * to this page, to be sure that the API will get only the latest and correct one.
   * *IMPORTANT*: the cookie must have all the flags as the one set by the API;
   * path in this case is mandatory to set.
   *
   * This needs to happen once per lifecycle of the page.
   */
  React.useEffect(() => {
    const creds = document.cookie
      .split(';')
      .filter(c => c.indexOf('cred-o') === 0);
    creds.forEach(c => {
      const [key] = c.split('=');
      document.cookie = `${key}=; expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/`;
    });
  }, []);

  /**
   * if we need to set up an OAuth flow, we need to set the cookie returned by
   * the API in the document. This cookie will be later used by the BE in the
   * redirect page set up in the 3rd party.
   *
   * This needs to happen once per lifecycle of the page to avoid resetting the
   * cookie to the value we got from the API before the successful auth.
   */
  const previousSpec = React.useRef<string | undefined>();
  React.useEffect(() => {
    if (
      connectResource &&
      connectResource.state &&
      connectResource.state.spec !== previousSpec.current
    ) {
      previousSpec.current = connectResource.state.spec;
      window.document.cookie = connectResource.state.spec;
    }
  }, [previousSpec, connectResource]);

  /**
   * the callback that's called by the connect button for oauth enabled connectors.
   * It will open a popup pointing to oauth-redirect.html file, that will call
   * the authCompleted callback containing the result of the authorization
   * process.
   *
   * If the popup can't be opened for any reason, an error toast is shown.
   */
  function connectOAuth() {
    try {
      /**
       * if we need to set up an OAuth flow, we need to set the cookie returned by
       * the API in the document. This cookie will be later used by the BE in the
       * redirect page set up in the 3rd party.
       */
      const popup = window.open(
        connectResource!.redirectUrl,
        'Connection popup',
        'width=600,height=400,resizable,scrollbars=yes,status=yes'
      );
      if (!popup) {
        pushNotification(
          `Your browser is preventing the application from opening the connection to ${connectorName} page.`,
          'error'
        );
      }
    } catch (e) {
      pushNotification(`Errors trying to connect: ${e.message}`, 'error');
    }
  }

  return { connectOAuth, isConnecting };
}
