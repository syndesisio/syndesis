import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { I18nextProvider, NamespacesConsumer } from 'react-i18next';
import { BrowserRouter as Router } from 'react-router-dom';
import { App } from './app';
import i18n from './i18n';
import './index.css';
import { ConnectionsModule } from './modules/connections';
import { CustomizationsModule } from './modules/customizations';
import { DashboardModule } from './modules/dashboard';
import { DataModule } from './modules/data';
import { IntegrationsModule } from './modules/integrations';
import registerServiceWorker from './registerServiceWorker';

ReactDOM.render(
  <Router>
    <I18nextProvider i18n={i18n}>
      <NamespacesConsumer ns={['shared']}>
        {t => (
          <App
            routes={[
              {
                component: DashboardModule,
                exact: true,
                icon: 'fa fa-tachometer',
                label: t('Home'),
                to: '/',
              },
              {
                component: IntegrationsModule,
                icon: 'pficon pficon-integration',
                label: t('Integrations'),
                to: '/integrations',
              },
              {
                component: ConnectionsModule,
                icon: 'pficon pficon-plugged',
                label: t('Connections'),
                to: '/connections',
              },
              {
                component: CustomizationsModule,
                icon: 'fa fa-cube',
                label: t('Customizations'),
                to: '/customizations',
              },
              {
                component: DataModule,
                icon: 'fa fa-database',
                label: t('Data'),
                to: '/data',
              },
              // {
              // component: TODO,
              // icon: 'pficon pficon-settings',
              // label: t('Settings'),
              // to: '/settings',
              // },
            ]}
          />
        )}
      </NamespacesConsumer>
    </I18nextProvider>
  </Router>,
  document.getElementById('root') as HTMLElement
);
registerServiceWorker();
