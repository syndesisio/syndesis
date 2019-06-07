import {
  useConnector,
  useConnectorCredentials,
  useConnectorCredentialsConnect,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Connector } from '@syndesis/models';
import {
  ConnectionCreatorLayout,
  ConnectorAuthorization,
  ConnectorConfigurationForm,
  PageLoader,
  PageSection,
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../../app';
import { ApiError, PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import {
  ConnectionCreatorBreadSteps,
  WithConnectorForm,
} from '../../components';
import { ConnectionCreatorBreadcrumb } from '../../components/ConnectionCreatorBreadcrumb';
import resolvers from '../../resolvers';
import routes from '../../routes';

export interface IConfigurationPageRouteParams {
  connectorId: string;
}

export interface IConfigurationPageRouteState {
  connector: Connector;
}

/**
 * A page to set up the data required for a connector. A connector can be oauth
 * enabled or not.
 *
 * For non oauth enabled connectors, we simply display a form built on top of the
 * connector's properties.
 *
 * For oauth enabled connectors, we need to set up the oauth flow. It works like this:
 *
 * ConfigurationPage -> 3rd party authorization page (3rd party BE <-> Syndesis BE auth API) -> Syndesis BE redirect callback -> oauth-popup.html -> ConfigurationPage
 *
 * Basically we tell the BE that we want the flow to end up opening the url where
 * the oauth-popup.html is hosted. That file will call a global function that we
 * setup on page mount that to pass back the authorization result - which can be
 * either successful or not - and the updated cookie. In case of success, we pass
 * this cookie to the review page to allow the save connector API to retrieve the
 * right data to set up this connector.
 */
export const ConfigurationPage: React.FunctionComponent = () => {
  const { t } = useTranslation(['connections', 'shared']);
  const { params, state, history } = useRouteData<
    IConfigurationPageRouteParams,
    IConfigurationPageRouteState
  >();
  const { pushNotification } = React.useContext(UIContext);
  /**
   * retrieve the connector from the BE in case we don't have it in the route state
   */
  const { hasData, error: errorConnector, resource: connector } = useConnector(
    params.connectorId,
    state.connector
  );
  /**
   * check if the connector requires some kind of connection to a 3rd party. We
   * just check for a valid resource and not it's content since we support only
   * oauth2 for the moment.
   */
  const {
    loading: loadingCredentials,
    error: errorCredentials,
    resource: acquisitionMethod,
  } = useConnectorCredentials(params.connectorId);
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
    params.connectorId,
    `${process.env.PUBLIC_URL}/oauth-redirect.html`
  );

  /**
   * if we need to set up an OAuth flow, we need to set the cookie returned by
   * the API in the document. This cookie will be later used by the BE in the
   * redirect page set up in the 3rd party.
   */
  if (connectResource) {
    window.document.cookie = connectResource.state.spec;
  }

  /**
   * create a callback reference to a function that will be globally available and
   * that will be used by the oauth2 popup landing page to pass back to this the
   * result of the connection
   */
  const authCompleted = React.useCallback(
    (authState: string, cookie: string) => {
      // document.cookie = `${cookie};path=/;secure`;
      try {
        const auth = JSON.parse(decodeURIComponent(authState));
        if (auth.status === 'FAILURE') {
          throw new Error(auth.message);
        }
        pushNotification(auth.message, 'success');
        history.push(
          resolvers.create.review({
            connector,
            cookie,
          })
        );
      } catch (e) {
        pushNotification(`Connection failed: ${e.message}`, 'error');
      }
    },
    [pushNotification, connector, history]
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
   * path in this case is mandatory to set
   */
  React.useEffect(() => {
    const creds = document.cookie
      .split(';')
      .filter(c => c.indexOf('cred-o2') === 0);
    creds.forEach(c => {
      const [key] = c.split('=');
      document.cookie = `${key}=; expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/`;
    });
  }, []);

  /**
   * the callback that's called by the configuration form for non oauth enabled
   * connectors.
   *
   * @param configuredProperties
   */
  const onSave = (configuredProperties: { [key: string]: string }) => {
    history.push(
      resolvers.create.review({
        configuredProperties,
        connector,
      })
    );
  };

  /**
   * the callback that's called by the connect button for oauth enabled connectors.
   * It will open a popup pointing to the url provided by the connectResource.
   * Eventually that popup will end up loading the oauth-redirect.html page, that
   * will call the authCompleted callback containing the result of the authorization
   * process.
   *
   * If the popup can't be opened for any reason, an error toast is shown.
   */
  const onConnect = () => {
    const popup = window.open(
      connectResource!.redirectUrl,
      'Connection popup',
      'width=600,height=400,resizable,scrollbars=yes,status=yes'
    );
    if (!popup) {
      pushNotification(
        `Your browser is preventing the application from opening the connection to ${
          connector.name
        } page.`,
        'error'
      );
    }
  };

  return (
    <WithLeaveConfirmation
      i18nTitle={t('connections:create:unsavedChangesTitle')}
      i18nConfirmationMessage={t('connections:create:unsavedChangesMessage')}
      shouldDisplayDialog={(location: H.LocationDescriptor) => {
        const url =
          typeof location === 'string' ? location : location.pathname!;
        return !url.startsWith(routes.create.root);
      }}
    >
      {() => (
        <>
          <PageTitle title={t('connections:create:configure:title')} />
          <ConnectionCreatorBreadcrumb cancelHref={resolvers.connections()} />
          <ConnectionCreatorLayout
            header={<ConnectionCreatorBreadSteps step={2} />}
            content={
              <PageSection>
                <WithLoader
                  error={(errorConnector || errorCredentials) !== false}
                  loading={!hasData || loadingCredentials}
                  loaderChildren={<PageLoader />}
                  errorChildren={<ApiError />}
                >
                  {() =>
                    acquisitionMethod ? (
                      <ConnectorAuthorization
                        i18nTitle={t(
                          'connections:create:configure:configurationTitle',
                          { name: connector.name }
                        )}
                        i18nDescription={t(
                          'connections:create:configure:configurationDescription',
                          { name: connector.name }
                        )}
                        i18nConnectButton={t(
                          'connections:create:configure:configurationButton',
                          { name: connector.name }
                        )}
                        isConnecting={isConnecting}
                        onConnect={onConnect}
                      />
                    ) : (
                      <WithConnectorForm connector={connector} onSave={onSave}>
                        {({
                          fields,
                          handleSubmit,
                          validationResults,
                          submitForm,
                          isSubmitting,
                          isValid,
                          isValidating,
                          validateForm,
                        }) => (
                          <ConnectorConfigurationForm
                            i18nFormTitle={connector.name}
                            handleSubmit={handleSubmit}
                            backHref={resolvers.create.selectConnector()}
                            onNext={submitForm}
                            isNextDisabled={isSubmitting || !isValid}
                            isNextLoading={isSubmitting}
                            isValidating={isValidating}
                            onValidate={(ev: React.FormEvent) => {
                              ev.preventDefault();
                              validateForm();
                            }}
                            validationResults={validationResults}
                            isLastStep={false}
                            i18nSave={t('shared:Save')}
                            i18nNext={t('shared:Next')}
                          >
                            {fields}
                          </ConnectorConfigurationForm>
                        )}
                      </WithConnectorForm>
                    )
                  }
                </WithLoader>
              </PageSection>
            }
          />
        </>
      )}
    </WithLeaveConfirmation>
  );
};
