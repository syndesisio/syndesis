import { useVirtualizationConnectionSchema } from '@syndesis/api';
import { SchemaNode, SchemaNodeInfo } from '@syndesis/models';
import {
  ConnectionSchemaList,
  ConnectionSchemaListItem,
  ConnectionSchemaListSkeleton,
  SchemaNodeListItem,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { ApiError } from '../../../shared';
import resolvers from '../../resolvers';
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
  onNodeSelected: (
    connectionName: string,
    name: string,
    teiidName: string,
    nodePath: string[]
  ) => void;
  onNodeDeselected: (connectionName: string, teiidName: string) => void;
  selectedSchemaNodes: SchemaNodeInfo[];
}

export const ConnectionSchemaContent: React.FunctionComponent<IConnectionSchemaContentProps> = props => {
  const { t } = useTranslation(['data']);

  const handleSourceSelectionChange = async (
    connectionName: string,
    name: string,
    teiidName: string,
    nodePath: string[],
    selected: boolean
  ) => {
    if (selected) {
      props.onNodeSelected(connectionName, name, teiidName, nodePath);
    } else {
      props.onNodeDeselected(connectionName, teiidName);
    }
  };

  const isConnctionSelected = (cName: string): boolean => {
    let returnVal = false;
    for (const tables of props.selectedSchemaNodes) {
      if (tables.connectionName === cName) {
        returnVal = true;
        break;
      }
    }
    return returnVal;
  };

  const isTableSelected = (cName: string, teiidName?: string): boolean => {
    let returnVal = false;
    for (const tables of props.selectedSchemaNodes) {
      if (tables.connectionName === cName) {
        if (tables.teiidName === teiidName) {
          returnVal = true;
          break;
        }
      }
    }
    return returnVal;
  };

  const {
    resource: schema,
    hasData: hasSchema,
    error,
  } = useVirtualizationConnectionSchema();

  // Root nodes of the response contain the connection names
  const connNames = getConnectionNames(schema);

  return (
    <ConnectionSchemaList
      i18nEmptyStateInfo={t('activeConnectionsEmptyStateInfo')}
      i18nEmptyStateTitle={t('activeConnectionsEmptyStateTitle')}
      i18nLinkCreateConnection={t('shared:linkCreateConnection')}
      hasListData={connNames.length > 0}
      linkToConnectionCreate={resolvers.connections.create.selectConnector()}
    >
      <WithLoader
        error={error !== false}
        loading={!hasSchema}
        loaderChildren={<ConnectionSchemaListSkeleton width={800} />}
        errorChildren={<ApiError error={error as Error} />}
      >
        {() =>
          connNames.map((cName: string, index: number) => {
            // get schema nodes for the connection
            const srcInfos = getSchemaNodeInfos(schema, cName);
            return (
              <ConnectionSchemaListItem
                key={index}
                connectionName={cName}
                connectionDescription={''}
                haveSelectedSource={
                  props.selectedSchemaNodes[0]
                    ? isConnctionSelected(cName)
                    : false
                }
                // tslint:disable-next-line: no-shadowed-variable
                children={srcInfos.map((info, index) => (
                  <SchemaNodeListItem
                    key={index}
                    name={info.name}
                    teiidName={info.teiidName}
                    connectionName={info.connectionName}
                    nodePath={info.nodePath}
                    selected={isTableSelected(info.connectionName, info.name)}
                    onSelectionChanged={handleSourceSelectionChange}
                  />
                ))}
              />
            );
          })
        }
      </WithLoader>
    </ConnectionSchemaList>
  );
};
