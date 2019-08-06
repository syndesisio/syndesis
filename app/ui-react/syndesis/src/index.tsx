import {
  ApiContext,
  ServerEventsContext,
  WithServerEvents,
} from '@syndesis/api';
// Don't move this line, otherwise we anger the pf4
import { App, IAppRoute, IAppRouteWithChildrens, WithConfig } from './app';
// tslint:disable-next-line:ordered-imports
import { createBrowserHistory } from '@syndesis/history';
import { UnrecoverableError } from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { I18nextProvider, Translation } from 'react-i18next';
import { Router } from 'react-router-dom';
import i18n from './i18n';
import { ApiClientConnectorsModule } from './modules/apiClientConnectors';
import { ConnectionsModule } from './modules/connections';
import { DashboardModule } from './modules/dashboard';
import { DataModule } from './modules/data';
import { ExtensionsModule } from './modules/extensions';
import { IntegrationsModule } from './modules/integrations';
import routes from './modules/routes';
import { SettingsModule } from './modules/settings';
import { SupportModule } from './modules/support';
import { unregister } from './registerServiceWorker';

ReactDOM.render(
  <Router history={createBrowserHistory()}>
    <I18nextProvider i18n={i18n}>
      <WithConfig>
        {({ config, loading, error }) => (
          <WithLoader
            loading={loading}
            error={error}
            loaderChildren={<span />}
            errorChildren={
              <Translation ns={['shared']}>
                {t => (
                  <UnrecoverableError
                    i18nTitle={t('shared:error.title')}
                    i18nInfo={t('shared:error.info')}
                    i18nHelp={t('shared:error.help')}
                    i18nRefreshLabel={t('shared:error.refreshButton')}
                    i18nReportIssue={t('shared:error.reportIssueButton')}
                    i18nShowErrorInfoLabel={t(
                      'shared:error.showErrorInfoButton'
                    )}
                  />
                )}
              </Translation>
            }
            minWait={1000}
          >
            {() => (
              <ApiContext.Provider
                value={{
                  apiUri: `${config!.apiBase}${config!.apiEndpoint}`,
                  dvApiUri: `${config!.apiBase}${config!.datavirt.dvUrl}`,
                  headers: { 'SYNDESIS-XSRF-TOKEN': 'awesome' },
                }}
              >
                <ApiContext.Consumer>
                  {({ apiUri, headers }) => (
                    <WithServerEvents apiUri={apiUri} headers={headers}>
                      {functions => (
                        <ServerEventsContext.Provider value={functions}>
                          <App
                            config={config!}
                            routes={[
                              {
                                component: DashboardModule,
                                exact: true,
                                label: 'Home',
                                to: routes.dashboard.root,
                              } as IAppRoute,
                              {
                                component: IntegrationsModule,
                                label: 'Integrations',
                                to: routes.integrations.list,
                              } as IAppRoute,
                              {
                                component: ConnectionsModule,
                                label: 'Connections',
                                to: routes.connections.connections,
                              } as IAppRoute,
                              {
                                childrens: [
                                  {
                                    component: ApiClientConnectorsModule,
                                    label: 'API Client Connectors',
                                    to: routes.apiClientConnectors.list,
                                  } as IAppRoute,
                                  {
                                    component: ExtensionsModule,
                                    label: 'Extensions',
                                    to: routes.extensions.list,
                                  } as IAppRoute,
                                ],
                                label: 'Customizations',
                              } as IAppRouteWithChildrens,
                              {
                                component: DataModule,
                                label: 'Data',
                                to: routes.data.root,
                              } as IAppRoute,
                              {
                                component: SettingsModule,
                                label: 'Settings',
                                to: routes.settings.root,
                              } as IAppRoute,
                              {
                                component: SupportModule,
                                label: 'Support',
                                to: routes.support.root,
                              } as IAppRoute,
                            ]}
                          />
                        </ServerEventsContext.Provider>
                      )}
                    </WithServerEvents>
                  )}
                </ApiContext.Consumer>
              </ApiContext.Provider>
            )}
          </WithLoader>
        )}
      </WithConfig>
    </I18nextProvider>
  </Router>,
  document.getElementById('root') as HTMLElement
);

unregister();
