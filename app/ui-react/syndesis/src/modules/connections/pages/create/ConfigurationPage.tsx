import {
  useConnector,
  useConnectorCredentials,
  useConnectorCredentialsConnect,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import { Connector } from '@syndesis/models';
import {
  ButtonLink,
  ConnectionCreatorLayout,
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

export const ConfigurationPage: React.FunctionComponent = () => {
  const { t } = useTranslation(['connections', 'shared']);
  const { params, state, history } = useRouteData<
    IConfigurationPageRouteParams,
    IConfigurationPageRouteState
  >();
  const { hasData, error: errorConnector, resource: connector } = useConnector(
    params.connectorId,
    state.connector
  );
  const {
    loading: loadingCredentials,
    error: errorCredentials,
    resource: acquisitionMethod,
  } = useConnectorCredentials(params.connectorId);
  const { pushNotification } = React.useContext(UIContext);
  const {
    loading: isConnecting,
    error: errorConnecting,
    resource: connectResource,
  } = useConnectorCredentialsConnect(
    params.connectorId,
    `${process.env.PUBLIC_URL}/oauth-redirect.html`
  );
  if (connectResource) {
    window.document.cookie = connectResource.state.spec;
  }

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

  React.useEffect(() => {
    (window as any).authCompleted = authCompleted;

    return () => {
      delete (window as any).authCompleted;
    };
  }, [authCompleted]);

  React.useEffect(() => {
    const creds = document.cookie
      .split(';')
      .filter(c => c.indexOf('cred-o2') === 0);
    creds.forEach(c => {
      const [key] = c.split('=');
      document.cookie = `${key}=; expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/`;
    });
  }, []);

  const onSave = (configuredProperties: { [key: string]: string }) => {
    history.push(
      resolvers.create.review({
        configuredProperties,
        connector,
      })
    );
  };

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
                      <>
                        {errorConnecting && (
                          <pre>{JSON.stringify(errorConnecting, null, 2)}</pre>
                        )}
                        <ButtonLink disabled={isConnecting} onClick={onConnect}>
                          Connect
                        </ButtonLink>
                      </>
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
