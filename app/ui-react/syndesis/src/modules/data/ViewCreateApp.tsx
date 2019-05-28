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
import { SelectNamePage, SelectSourcesPage } from './pages/viewCreate';

export interface IViewCreateAppRouteState {
  virtualization: RestDataService;
}

export default class ViewCreateApp extends React.Component {
  public render() {
    return (
      <WithRouteData<null, IViewCreateAppRouteState>>
        {(_, { virtualization }) => (
          <WithClosedNavigation>
            <Breadcrumb>
              <Link
                data-testid={'view-create-app-home-link'}
                to={resolvers.dashboard.root()}
              >
                {i18n.t('shared:Home')}
              </Link>
              <Link
                data-testid={'view-create-app-virtualizations-link'}
                to={resolvers.data.root()}
              >
                {i18n.t('shared:DataVirtualizations')}
              </Link>
              <Link
                data-testid={'view-create-app-virtualization-link'}
                to={resolvers.data.virtualizations.views.root({
                  virtualization,
                })}
              >
                {virtualization.keng__id}
              </Link>
              <span>{i18n.t('data:virtualization.createView')}</span>
            </Breadcrumb>
            <Switch>
              {/* step 1 */}
              <Route
                path={
                  routes.data.virtualizations.virtualization.views.createView
                    .selectSources
                }
                exact={true}
                component={SelectSourcesPage}
              />
              {/* step 2 */}
              <Route
                path={
                  routes.data.virtualizations.virtualization.views.createView
                    .selectName
                }
                exact={true}
                component={SelectNamePage}
              />
            </Switch>
          </WithClosedNavigation>
        )}
      </WithRouteData>
    );
  }
}
