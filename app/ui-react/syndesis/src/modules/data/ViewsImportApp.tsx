import { RestDataService } from '@syndesis/models';
import { Breadcrumb } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
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

export default class ViewsImportApp extends React.Component {
  public render() {
    return (
      <WithRouteData<null, IViewsImportAppRouteState>>
        {(_, { virtualization }) => (
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
                  virtualization,
                })}
              >
                {virtualization.keng__id}
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
        )}
      </WithRouteData>
    );
  }
}
