import { SchemaNodeInfo, Virtualization } from '@syndesis/models';
import { Breadcrumb } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Route, Switch } from 'react-router';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../shared';
import resolvers from '../resolvers';
import routes from '../routes';
import { SelectNamePage, SelectSourcesPage } from './pages/viewCreate';

export interface IViewCreateAppRouteState {
  virtualization: Virtualization;
}

export const ViewCreateApp: React.FunctionComponent = () => {
  const { t } = useTranslation(['data', 'shared']);
  const { state } = useRouteData<null, IViewCreateAppRouteState>();

  const [selectedSchemaNodes, setSelectedSchemaNodes] = React.useState<
    SchemaNodeInfo[]
  >([]);

  const handleNodeSelected = async (
    connName: string,
    isVirtualizationSchema: boolean,
    name: string,
    teiidName: string,
    nodePath: string[]
  ) => {
    const srcInfo = {
      connectionName: connName,
      isVirtualizationSchema,
      name,
      nodePath,
      teiidName,
    } as SchemaNodeInfo;

    const currentNodes = selectedSchemaNodes.slice();
    currentNodes.push(srcInfo);
    setSelectedSchemaNodes(currentNodes);
  };

  const handleNodeDeselected = async (
    connectionName: string,
    teiidName: string
  ) => {
    const tempArray = selectedSchemaNodes.slice();

    // find array index with element matching teiidName and connectionName
    const index = tempArray.findIndex(
      element => element.teiidName === teiidName && element.connectionName === connectionName
    );

    if (index > -1) {
      tempArray.splice(index, 1);
    }
    setSelectedSchemaNodes(tempArray);
  };

  return (
    <WithClosedNavigation>
      <Breadcrumb>
        <Link
          data-testid={'view-create-app-home-link'}
          to={resolvers.dashboard.root()}
        >
          {t('shared:Home')}
        </Link>
        <Link
          data-testid={'view-create-app-virtualizations-link'}
          to={resolvers.data.root()}
        >
          {t('shared:Data')}
        </Link>
        <Link
          data-testid={'view-create-app-virtualization-link'}
          to={resolvers.data.virtualizations.views.root({
            virtualization: state.virtualization,
          })}
        >
          {t('virtualizationNameBreadcrumb', {
            name: state.virtualization.name,
          })}
        </Link>
        <span>{t('createView')}</span>
      </Breadcrumb>
      <Switch>
        {/* step 1 */}
        <Route
          path={
            routes.data.virtualizations.virtualization.views.createView
              .selectSources
          }
          exact={true}
          render={() => (
            <SelectSourcesPage
              selectedSchemaNodes={selectedSchemaNodes}
              handleNodeSelected={handleNodeSelected}
              handleNodeDeselected={handleNodeDeselected}
            />
          )}
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
  );
};
