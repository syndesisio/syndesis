import { RestDataService, SchemaNodeInfo } from '@syndesis/models';
import { ViewCreateLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
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

export interface ISelectSourcesPageState {
  selectedSchemaNodes: SchemaNodeInfo[];
}

export class SelectSourcesPage extends React.Component<
  {},
  ISelectSourcesPageState
> {
  public constructor(props: {}) {
    super(props);
    this.state = {
      selectedSchemaNodes: [], // initial selected sources
    };
    this.handleNodeSelected = this.handleNodeSelected.bind(this);
    this.handleNodeDeselected = this.handleNodeDeselected.bind(this);
  }

  public handleNodeSelected(connName: string, nodePath: string) {
    const srcInfo = {
      connectionName: connName,
      sourceName: getNodeName(nodePath),
      sourcePath: nodePath,
    } as SchemaNodeInfo;
    this.setState({
      selectedSchemaNodes: [srcInfo],
    });
  }

  public handleNodeDeselected(connectionName: string, nodePath: string) {
    this.setState({
      selectedSchemaNodes: [],
    });
  }

  public render() {
    const schemaNodeInfo: SchemaNodeInfo = this.state.selectedSchemaNodes[0];
    return (
      <WithRouteData<ISelectSourcesRouteParams, ISelectSourcesRouteState>>
        {({ virtualizationId }, { virtualization }, { history }) => (
          <ViewCreateLayout
            header={<ViewCreateSteps step={1} />}
            content={
              <ConnectionSchemaContent
                onNodeSelected={this.handleNodeSelected}
                onNodeDeselected={this.handleNodeDeselected}
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
            isNextDisabled={this.state.selectedSchemaNodes.length < 1}
            isNextLoading={false}
            isLastStep={false}
          />
        )}
      </WithRouteData>
    );
  }
}
