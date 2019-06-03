import { WithVirtualizationConnectionSchema } from '@syndesis/api';
import { SchemaNode, SchemaNodeInfo } from '@syndesis/models';
import {
  ConnectionSchemaList,
  ConnectionSchemaListItem,
  ConnectionSchemaListSkeleton,
  SchemaNodeListItem,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { ApiError } from '../../../shared';
import { generateSchemaNodeInfos } from './VirtualizationUtils';

function getConnectionNames(schemaNodes: SchemaNode[]) {
  return schemaNodes
    .map(schemaNode => schemaNode.name)
    .sort((a, b) => a.localeCompare(b));
}

function getSchemaNodeInfos(schemaNodes: SchemaNode[], connName: string) {
  const schemaNodeInfos: SchemaNodeInfo[] = [];
  const rootNode = schemaNodes.find(node => node.name === connName);
  if (rootNode) {
    generateSchemaNodeInfos(schemaNodeInfos, rootNode, []);
  }
  return schemaNodeInfos;
}

export interface IConnectionSchemaContentProps {
  onNodeSelected: (connectionName: string, nodePath: string) => void;
  onNodeDeselected: (connectionName: string, nodePath: string) => void;
}

export class ConnectionSchemaContent extends React.Component<
  IConnectionSchemaContentProps
> {
  public constructor(props: IConnectionSchemaContentProps) {
    super(props);
    this.handleSourceSelectionChange = this.handleSourceSelectionChange.bind(
      this
    );
  }

  public handleSourceSelectionChange(
    connectionName: string,
    nodePath: string,
    selected: boolean
  ) {
    if (selected) {
      this.props.onNodeSelected(connectionName, nodePath);
    } else {
      this.props.onNodeDeselected(connectionName, nodePath);
    }
  }

  public render() {
    return (
      <WithVirtualizationConnectionSchema>
        {({ data, hasData, error }) => {
          // Root nodes of the response contain the connection names
          const connNames = getConnectionNames(data);
          return (
            <Translation ns={['data', 'shared']}>
              {t => (
                <ConnectionSchemaList
                  i18nEmptyStateInfo={t(
                    'virtualization.activeConnectionsEmptyStateInfo'
                  )}
                  i18nEmptyStateTitle={t(
                    'virtualization.activeConnectionsEmptyStateTitle'
                  )}
                  hasListData={connNames.length > 0}
                >
                  <WithLoader
                    error={error}
                    loading={!hasData}
                    loaderChildren={
                      <ConnectionSchemaListSkeleton
                        width={800}
                        style={{
                          backgroundColor: '#FFF',
                          marginTop: 30,
                        }}
                      />
                    }
                    errorChildren={<ApiError />}
                  >
                    {() =>
                      connNames.map((cName: string, index: number) => {
                        // get schema nodes for the connection
                        const srcInfos = getSchemaNodeInfos(data, cName);
                        return (
                          <ConnectionSchemaListItem
                            key={index}
                            connectionName={cName}
                            connectionDescription={''}
                            // tslint:disable-next-line: no-shadowed-variable
                            children={srcInfos.map((info, index) => (
                              <SchemaNodeListItem
                                key={index}
                                name={info.sourceName}
                                connectionName={info.connectionName}
                                schemaPath={info.sourcePath}
                                selected={false}
                                onSelectionChanged={
                                  this.handleSourceSelectionChange
                                }
                              />
                            ))}
                          />
                        );
                      })
                    }
                  </WithLoader>
                </ConnectionSchemaList>
              )}
            </Translation>
          );
        }}
      </WithVirtualizationConnectionSchema>
    );
  }
}
