import {
  ApiContext,
  ServerEventsContext,
  WithServerEvents,
} from '@syndesis/api';
import { createBrowserHistory } from '@syndesis/history';
import { Loader, UnrecoverableError } from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { I18nextProvider, Translation } from 'react-i18next';
import { Router } from 'react-router-dom';
import { App } from './app';
import { WithConfig } from './app/WithConfig';
import i18n from './i18n';
import './index.css';
import { ConnectionsModule } from './modules/connections';
import { CustomizationsModule } from './modules/customizations';
import { DashboardModule } from './modules/dashboard';
import { DataModule } from './modules/data';
import { IntegrationsModule } from './modules/integrations';
import { SettingsModule } from './modules/settings';
import registerServiceWorker from './registerServiceWorker';

ReactDOM.render(
  <Router history={createBrowserHistory()}>
    <I18nextProvider i18n={i18n}>
      <WithConfig>
        {({ config, loading, error }) => (
          <WithLoader
            loading={loading}
            error={error}
            loaderChildren={<Loader />}
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
                                icon: 'fa fa-tachometer-alt',
                                label: 'Home',
                                to: '/',
                              },
                              {
                                component: IntegrationsModule,
                                icon: 'pficon pficon-integration',
                                label: 'Integrations',
                                to: '/integrations',
                              },
                              {
                                component: ConnectionsModule,
                                icon: 'pficon pficon-plugged',
                                label: 'Connections',
                                to: '/connections',
                              },
                              {
                                component: CustomizationsModule,
                                icon: 'fa fa-cube',
                                label: 'Customizations',
                                to: '/customizations',
                              },
                              {
                                component: DataModule,
                                icon: 'fa fa-database',
                                label: 'Data',
                                to: '/data',
                              },
                              {
                                component: SettingsModule,
                                icon: 'pficon pficon-settings',
                                label: 'Settings',
                                to: '/settings',
                              },
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
registerServiceWorker();
