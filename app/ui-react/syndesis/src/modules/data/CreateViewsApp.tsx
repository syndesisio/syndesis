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
import { SelectConnectionPage, SelectViewsPage } from './pages/views';

export interface ICreateViewsAppRouteState {
  virtualization: RestDataService;
}

export default class CreateViewsApp extends React.Component {
  public render() {
    return (
      <WithRouteData<null, ICreateViewsAppRouteState>>
        {(_, { virtualization }) => (
          <WithClosedNavigation>
            <Breadcrumb>
              <Link to={resolvers.dashboard.root()}>
                {i18n.t('shared:Home')}
              </Link>
              <Link to={resolvers.data.root()}>
                {i18n.t('shared:DataVirtualizations')}
              </Link>
              <Link
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
