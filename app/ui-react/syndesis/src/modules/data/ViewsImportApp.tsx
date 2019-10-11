import { Virtualization } from '@syndesis/models';
import { Breadcrumb } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Route, Switch } from 'react-router';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../shared';
import resolvers from '../resolvers';
import routes from '../routes';
import { SelectConnectionPage, SelectViewsPage } from './pages/viewsImport';

export interface IViewsImportAppRouteState {
  virtualization: Virtualization;
}

export const ViewsImportApp: React.FunctionComponent = () => {
  const { t } = useTranslation(['data', 'shared']);
  const { state } = useRouteData<null, IViewsImportAppRouteState>();

  return (
    <WithClosedNavigation>
      <Breadcrumb>
        <Link
          data-testid={'views-import-app-home-link'}
          to={resolvers.dashboard.root()}
        >
          {t('shared:Home')}
        </Link>
        <Link
          data-testid={'views-import-app-virtualizations-link'}
          to={resolvers.data.root()}
        >
          {t('shared:DataVirtualizations')}
        </Link>
        <Link
          data-testid={'views-import-app-virtualization-link'}
          to={resolvers.data.virtualizations.views.root({
            virtualization: state.virtualization,
          })}
        >
          {state.virtualization.keng__id}
        </Link>
        <span>{t('importDataSource')}</span>
      </Breadcrumb>
      <Switch>
        {/* step 1 */}
        <Route
          path={
            routes.data.virtualizations.virtualization.views.importSource
              .selectConnection
          }
          exact={true}
          component={SelectConnectionPage}
        />
        {/* step 2 */}
        <Route
          path={
            routes.data.virtualizations.virtualization.views.importSource
              .selectViews
          }
          exact={true}
          component={SelectViewsPage}
        />
      </Switch>
    </WithClosedNavigation>
  );
};
