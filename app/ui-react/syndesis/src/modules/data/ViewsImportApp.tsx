import { ViewInfo, Virtualization } from '@syndesis/models';
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

  const [selectedConnection, setSelectedConnection] = React.useState('');

  const handleConnectionSelectionChanged = async (
    name: string,
    selected: boolean
  ) => {
    const selConn = selected ? name : '';
    setSelectedConnection(selConn);
    clearViewSelection();
  };

  const [selectedViews, setSelectedViews] = React.useState<ViewInfo[]>([]);

  const handleAddView = async (view: ViewInfo) => {
    const currentViews = selectedViews.slice();
    currentViews.push(view);
    setSelectedViews(currentViews);
  };

  const handleRemoveView = async (viewName: string) => {
    const currentViews = selectedViews.slice();
    const index = currentViews.findIndex(view => view.viewName === viewName);

    if (index !== -1) {
      currentViews.splice(index, 1);
    }
    setSelectedViews(currentViews);
  };

  const handleSelectAll = (isSelected: boolean, AllViewInfo?:ViewInfo[] ) =>{
    if(isSelected && AllViewInfo){
     setSelectedViews(AllViewInfo)
    }else{
      clearViewSelection();
    }
  }

  const clearViewSelection = () => {
    setSelectedViews([]);
  };

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
          {t('shared:Data')}
        </Link>
        <Link
          data-testid={'views-import-app-virtualization-link'}
          to={resolvers.data.virtualizations.views.root({
            virtualization: state.virtualization,
          })}
        >
          {t('virtualizationNameBreadcrumb', {
            name: state.virtualization.name,
          })}
        </Link>
        <span>{t('importViews')}</span>
      </Breadcrumb>
      <Switch>
        {/* step 1 */}
        <Route
          path={
            routes.data.virtualizations.virtualization.views.importSource
              .selectConnection
          }
          exact={true}
          render={() => (
            <SelectConnectionPage
              selectedConnection={selectedConnection}
              handleConnectionSelectionChanged={
                handleConnectionSelectionChanged
              }
            />
          )}
        />
        {/* step 2 */}
        <Route
          path={
            routes.data.virtualizations.virtualization.views.importSource
              .selectViews
          }
          exact={true}
          render={() => (
            <SelectViewsPage
              selectedViews={selectedViews}
              handleAddView={handleAddView}
              handleRemoveView={handleRemoveView}
              handleSelectAll={handleSelectAll}
            />
          )}
        />
      </Switch>
    </WithClosedNavigation>
  );
};
