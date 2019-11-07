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
    name: string,
    teiidName: string,
    nodePath: string[]
  ) => {
    const srcInfo = {
      connectionName: connName,
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
    const index = getIndex(teiidName, tempArray, 'teiidName');
    tempArray.splice(index, 1);
    setSelectedSchemaNodes(tempArray);
  };

  const getIndex = (value: string, arr: SchemaNodeInfo[], prop: string) => {
    for (let i = 0; i < arr.length; i++) {
      if (arr[i][prop] === value) {
        return i;
      }
    }
    return -1; // to handle the case where the value doesn't exist
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
          {t('shared:DataVirtualizations')}
        </Link>
        <Link
          data-testid={'view-create-app-virtualization-link'}
          to={resolvers.data.virtualizations.views.root({
            virtualization: state.virtualization,
          })}
        >
          {state.virtualization.name}
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
