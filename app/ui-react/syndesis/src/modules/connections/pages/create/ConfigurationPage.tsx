import { useConnector, useConnectorCredentials } from '@syndesis/api';
import * as H from '@syndesis/history';
import { IConnector } from '@syndesis/models';
import {
  ConnectionCreatorLayout,
  ConnectionSetupOAuthCard,
  ConnectorAuthorization,
  ConnectorConfigurationForm,
  PageLoader,
  PageSection,
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { ApiError, PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import globalRoutes from '../../../routes';
import {
  ConnectionCreatorBreadSteps,
  WithConnectorForm,
} from '../../components';
import { ConnectionCreatorBreadcrumb } from '../../components/ConnectionCreatorBreadcrumb';
import resolvers from '../../resolvers';
import routes from '../../routes';
import { useOAuthFlow } from '../../useOAuthFlow';

export interface IConfigurationPageRouteParams {
  connectorId: string;
}

export interface IConfigurationPageRouteState {
  connector: IConnector;
}

interface IOAuthFlowProps {
  connectorId: string;
  connectorName: string;
  onSuccess: () => void;
}

const OAuthFlow: React.FunctionComponent<IOAuthFlowProps> = ({
  connectorId,
  connectorName,
  onSuccess,
}) => {
  const { t } = useTranslation(['connections', 'shared']);

  const { connectOAuth, isConnecting } = useOAuthFlow(
    connectorId,
    connectorName,
    onSuccess
  );

  return (
    <ConnectorAuthorization
      i18nTitle={t('connections:create:configure:configurationTitle', {
        name: connectorName,
      })}
      i18nDescription={t(
        'connections:create:configure:configurationDescription',
        { name: connectorName }
      )}
      i18nConnectButton={t('connections:create:configure:configurationButton', {
        name: connectorName,
      })}
      isConnecting={isConnecting}
      onConnect={connectOAuth}
    />
  );
};

/**
 * A page to set up the data required for a connector. A connector can be oauth
 * enabled or not.
 *
 * For non oauth enabled connectors, we simply display a form built on top of the
 * connector's properties.
 *
 * For oauth enabled connectors, we need to set up the oauth flow. It works like this:
 *
 * ConfigurationPage -> 3rd party authorization page (3rd party BE <-> Syndesis BE auth API) -> Syndesis BE redirect callback (which updates the cred-o* cookie) -> oauth-popup.html -> ConfigurationPage
 *
 * Basically we tell the BE that we want the flow to end up opening the url where
 * the oauth-popup.html is hosted. That file will call a global function that we
 * setup on page mount that will have to be called back with authorization result -
 * which can be either successful or not. In case of success, we redirect the user
 * to the review page.
 */
export const ConfigurationPage: React.FunctionComponent = () => {
  const { t } = useTranslation(['connections', 'shared']);
  const { params, state, history } = useRouteData<
    IConfigurationPageRouteParams,
    IConfigurationPageRouteState
  >();
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
   * gets called at the point when the OAuth flow successfully finishes, this is
   * when the callback from the OAuth authorization server is processed, and the
   * backend redirects to UI again, the same moment as when the OAuth popup is
   * about to be closed
   */
  const onOAuthSuccess = () => {
    // any configuredProperties that have already been configured on the
    // connector need to be passed to the connection for the connection
    // validation to function
    const configuredProperties = connector.configuredProperties;
    history.push(
      resolvers.create.review({
        configuredProperties,
        connector,
      })
    );
  };

  const supportsOAuth =
    acquisitionMethod &&
    acquisitionMethod.type &&
    acquisitionMethod.type.startsWith('OAUTH');
  const configuredForOAuth =
    acquisitionMethod && (acquisitionMethod.configured as boolean);

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
                  errorChildren={
                    <ApiError
                      error={(errorConnector || errorCredentials) as Error}
                    />
                  }
                >
                  {() => {
                    if (configuredForOAuth) {
                      return (
                        <OAuthFlow
                          connectorId={connector.id!}
                          connectorName={connector.name}
                          onSuccess={onOAuthSuccess}
                        />
                      );
                    }

                    if (supportsOAuth) {
                      return (
                        <ConnectionSetupOAuthCard
                          i18nTitle={t(
                            'connections:create:configure:configurationTitle',
                            {
                              name: connector.name,
                            }
                          )}
                          i18nDescription={t(
                            'connections:oauth:settingsMissing'
                          )}
                          i18nOAuthSettingsButton={t('shared:Settings')}
                          backHref={resolvers.create.selectConnector()}
                          oauthSettingsHref={
                            globalRoutes.settings.oauthApps.root
                          }
                        />
                      );
                    }

                    return (
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
                    );
                  }}
                </WithLoader>
              </PageSection>
            }
          />
        </>
      )}
    </WithLeaveConfirmation>
  );
};
