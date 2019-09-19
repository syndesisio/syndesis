import { RestDataService, SchemaNodeInfo } from '@syndesis/models';
import { ViewCreateLayout } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import resolvers from '../../../resolvers';
import { ConnectionSchemaContent, ViewCreateSteps } from '../../shared';

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
  const [selectedNodesCount, setSelectedNodesCount] = React.useState(0);

  const handleNodeSelected = async (connName: string, name: string, teiidName: string, nodePath: string[]) => {
    const srcInfo = {
      connectionName: connName,
      name,
      nodePath,
      teiidName,
     } as SchemaNodeInfo;

    const currentNodes = selectedSchemaNodes;
    currentNodes.push(srcInfo);
    setSelectedSchemaNodes(currentNodes);
    setSelectedNodesCount(currentNodes.length);
  }

  const handleNodeDeselected = async (connectionName: string, teiidName: string) => {
    const tempArray = selectedSchemaNodes;
    const index = getIndex(teiidName, tempArray, 'teiidName');
    tempArray.splice(index, 1);
    setSelectedSchemaNodes(tempArray);
    setSelectedNodesCount(tempArray.length);
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
      isNextDisabled={selectedNodesCount>1}
      isNextLoading={false}
      isLastStep={false}
    />
  );

}
