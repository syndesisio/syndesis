import { RestDataService } from '@syndesis/models';
import { Breadcrumb } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Route, Switch } from 'react-router';
import { Link } from 'react-router-dom';
import i18n from '../../i18n';
import { WithClosedNavigation } from '../../shared';
import resolvers from '../resolvers';
import routes from '../routes';
import { SelectConnectionPage, SelectViewsPage } from './pages/viewsImport';

export interface IViewsImportAppRouteState {
  virtualization: RestDataService;
}

export const ViewsImportApp: React.FunctionComponent = () => {
  const { state } = useRouteData<null, IViewsImportAppRouteState>();

  return (
    <WithClosedNavigation>
      <Breadcrumb>
        <Link
          data-testid={'views-import-app-home-link'}
          to={resolvers.dashboard.root()}
        >
          {i18n.t('shared:Home')}
        </Link>
        <Link
          data-testid={'views-import-app-virtualizations-link'}
          to={resolvers.data.root()}
        >
          {i18n.t('shared:DataVirtualizations')}
        </Link>
        <Link
          data-testid={'views-import-app-virtualization-link'}
          to={resolvers.data.virtualizations.views.root({
            virtualization: state.virtualization,
          })}
        >
          {state.virtualization.keng__id}
        </Link>
        <span>{i18n.t('data:virtualization.importDataSource')}</span>
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
