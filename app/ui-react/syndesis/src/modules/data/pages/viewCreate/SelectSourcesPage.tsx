import { RestDataService, SchemaNodeInfo } from '@syndesis/models';
import { ViewCreateLayout } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import resolvers from '../../../resolvers';
import { ConnectionSchemaContent, ViewCreateSteps } from '../../shared';
import { getNodeName } from '../../shared/VirtualizationUtils';

/**
 * @param virtualizationId - the ID of the virtualization for the wizard
 */
export interface ISelectSourcesRouteParams {
  virtualizationId: string;
}

/**
 * @param virtualization - the virtualization for the wizard.
 */
export interface ISelectSourcesRouteState {
  virtualization: RestDataService;
}

export const SelectSourcesPage: React.FunctionComponent = () => {

  const { state } = useRouteData<
    null,
    ISelectSourcesRouteState
  >();
  const [selectedSchemaNodes, setSelectedSchemaNodes] = React.useState<SchemaNodeInfo[]>([]);
  const [hasSelectedNodes, setHasSelectedNodes] = React.useState(false);

  const handleNodeSelected = async (connName: string, nodePath: string) => {
    const srcInfo = {
      connectionName: connName,
      sourceName: getNodeName(nodePath),
      sourcePath: nodePath,
    } as SchemaNodeInfo;

    const currentNodes = selectedSchemaNodes;
    currentNodes.push(srcInfo);
    setSelectedSchemaNodes(currentNodes);
    setHasSelectedNodes(currentNodes.length>0);
  }

  const handleNodeDeselected = async (connectionName: string, nodePath: string) => {
    const tempArray = selectedSchemaNodes;
    const index = getIndex(nodePath, tempArray, 'sourcePath');
    tempArray.splice(index, 1);
    setSelectedSchemaNodes(tempArray);
    setHasSelectedNodes(tempArray.length>0);
  }

  const getIndex = (value: string, arr: SchemaNodeInfo[], prop: string) => {
    for (let i = 0; i < arr.length; i++) {
      if (arr[i][prop] === value) {
        return i;
      }
    }
    return -1; // to handle the case where the value doesn't exist
  }

  const schemaNodeInfo: SchemaNodeInfo[] = selectedSchemaNodes;
  const virtualization = state.virtualization;

  return (
    <ViewCreateLayout
      header={<ViewCreateSteps step={1} />}
      content={
        <ConnectionSchemaContent
          onNodeSelected={handleNodeSelected}
          onNodeDeselected={handleNodeDeselected}
        />
      }
      cancelHref={resolvers.data.virtualizations.views.root({
        virtualization,
      })}
      nextHref={resolvers.data.virtualizations.views.createView.selectName(
        {
          schemaNodeInfo,
          virtualization,
        }
      )}
      isNextDisabled={!hasSelectedNodes}
      isNextLoading={false}
      isLastStep={false}
    />
  );

}
