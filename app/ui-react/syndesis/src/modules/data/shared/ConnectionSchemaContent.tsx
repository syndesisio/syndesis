import { useVirtualizationConnectionSchema } from '@syndesis/api';
import {
  Connection,
  SchemaNode,
  SchemaNodeInfo,
  VirtualizationSourceStatus,
} from '@syndesis/models';
import {
  ConnectionSchemaList,
  ConnectionSchemaListItem,
  ConnectionSchemaListSkeleton,
  SchemaNodeListItem,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { ApiError, EntityIcon } from '../../../shared';
import resolvers from '../../resolvers';
import {
  generateDvConnections,
  generateSchemaNodeInfos,
  getDvConnectionStatus,
  getDvConnectionStatusMessage,
  isDvConnectionLoading,
} from './VirtualizationUtils';

function getSortedConnections(
  connections: Connection[],
  dvSourceStatuses: VirtualizationSourceStatus[],
  isSortAscending: boolean
) {
  // Connections are adjusted to supply dvStatus and selection
  let sortedConnections = generateDvConnections(connections, dvSourceStatuses);

  sortedConnections = sortedConnections.sort((miA, miB) => {
    const left = isSortAscending ? miA : miB;
    const right = isSortAscending ? miB : miA;
    return left.name.localeCompare(right.name);
  });

  return sortedConnections;
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
  error: boolean;
  errorMessage?: string;
  loading: boolean;
  dvSourceStatuses: VirtualizationSourceStatus[];
  connections: Connection[];
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

  const isConnectionSelected = (cName: string): boolean => {
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
  const sortedConns = getSortedConnections(
    props.connections,
    props.dvSourceStatuses,
    true
  );

  return (
    <ConnectionSchemaList
      i18nEmptyStateInfo={t('activeConnectionsEmptyStateInfo')}
      i18nEmptyStateTitle={t('activeConnectionsEmptyStateTitle')}
      i18nLinkCreateConnection={t('shared:linkCreateConnection')}
      hasListData={sortedConns.length > 0}
      linkToConnectionCreate={resolvers.connections.create.selectConnector()}
      loading={props.loading}
    >
      <WithLoader
        error={props.error || error !== false}
        loading={props.error || !hasSchema}
        loaderChildren={<ConnectionSchemaListSkeleton width={800} />}
        errorChildren={
          <ApiError error={props.errorMessage || (error as Error)} />
        }
      >
        {() =>
          sortedConns.map((c, index) => {
            // get schema nodes for the connection
            const srcInfos = getSchemaNodeInfos(schema, c.name);
            return (
              <ConnectionSchemaListItem
                key={index}
                connectionName={c.name}
                connectionDescription={''}
                dvStatus={getDvConnectionStatus(c)}
                dvStatusTooltip={getDvConnectionStatusMessage(c)}
                haveSelectedSource={
                  props.selectedSchemaNodes[0]
                    ? isConnectionSelected(c.name)
                    : false
                }
                i18nRefreshInProgress={t('refreshInProgress')}
                icon={<EntityIcon entity={c} alt={c.name} width={23} />}
                loading={isDvConnectionLoading(c)}
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
