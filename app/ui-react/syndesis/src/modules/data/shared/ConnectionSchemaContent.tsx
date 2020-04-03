import { useVirtualizationConnectionSchema, useVirtualizationHelpers } from '@syndesis/api';
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
import { UIContext } from '../../../app';
import { ApiError, EntityIcon } from '../../../shared';
import resolvers from '../../resolvers';
import {
  generateDvConnections,
  generateSchemaNodeInfos,
  getDateAndTimeDisplay,
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

export interface ILastRefreshMessage {
  connectionName: string;
  message: string;
}

export interface IConnectionSchemaContentProps {
  connections: Connection[];
  dvSourceStatuses: VirtualizationSourceStatus[];
  error: boolean;
  errorMessage?: string;
  loading: boolean;
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

  /**
   * Context that broadcasts global notifications.
   */
  const { pushNotification } = React.useContext(UIContext);
  const [lastSchemaRefresh, setLastSchemaRefresh] = React.useState(0);

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
    read,
  } = useVirtualizationConnectionSchema();

  const {
    refreshConnectionSchema,
  } = useVirtualizationHelpers();

  // Root nodes of the response contain the connection names
  const sortedConns = getSortedConnections(
    props.connections,
    props.dvSourceStatuses,
    true
  );

  /**
   * Callback that triggers refresh of the connection schema
   * @param connectionName the name of the connection
   */
  const handleRefreshSchema = async (connectionName: string) => {
    const srcStatus = props.dvSourceStatuses.find(
      status => status.sourceName === connectionName
    );
    if (srcStatus) {
      try {
        pushNotification(
          t('refreshConnectionSchemaStarted', {
            name: connectionName,
          }),
          'info'
        );
        await refreshConnectionSchema(srcStatus.teiidName);
      } catch (error) {
        const details = error.message ? error.message : '';
        // inform user of error
        pushNotification(
          t('refreshConnectionSchemaFailed', {
            details,
            name: connectionName,
          }),
          'error'
        );
      }
    } else {
      const details = t('connectionNotFound');
      // connection not found
      pushNotification(
        t('refreshConnectionSchemaFailed', {
          details,
          name: connectionName,
        }),
        'error'
      );
    }
  };

  React.useEffect(() => {
    // If any connection lastLoad is more recent than lastRefresh - reload schema
    for (const dvSrcStatus of props.dvSourceStatuses) {
      const connLastLoad = dvSrcStatus.lastLoad;
      if (connLastLoad > lastSchemaRefresh) {
        read();
        setLastSchemaRefresh(connLastLoad);
      }
    }
  }, [props.dvSourceStatuses, lastSchemaRefresh, read, setLastSchemaRefresh]);

  const getConnectionLastRefreshMessage = (connName: string) => {
    const status = props.dvSourceStatuses.find(
      srcStatus => srcStatus.sourceName === connName
    );
    if (status) {
      return t('schemaLastRefresh', {
        refreshTime: getDateAndTimeDisplay(status.lastLoad),
      });
    }
    return '';
  };

  const getConnectionTeiidName = (connName: string) => {
    const status = props.dvSourceStatuses.find(
      srcStatus => srcStatus.sourceName === connName
    );
    return status ? status.teiidName : '';
  };

  return (
    <ConnectionSchemaList
      i18nEmptyStateInfo={t('activeConnectionsEmptyStateInfo')}
      i18nEmptyStateTitle={t('activeConnectionsEmptyStateTitle')}
      i18nLinkCreateConnection={t('shared:CreateConnection')}
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
            const srcInfos = getSchemaNodeInfos(schema, getConnectionTeiidName(c.name));
            return (
              <ConnectionSchemaListItem
                key={index}
                connectionName={c.name}
                connectionDescription={c.description}
                dvStatus={getDvConnectionStatus(c)}
                dvStatusMessage={getDvConnectionStatusMessage(c)}
                haveSelectedSource={
                  props.selectedSchemaNodes[0]
                    ? isConnectionSelected(c.name)
                    : false
                }
                i18nLastUpdatedMessage={getConnectionLastRefreshMessage(c.name)}
                i18nRefresh={t('Refresh')}
                i18nRefreshInProgress={t('refreshInProgress')}
                i18nStatusErrorPopoverLink={t('connectionStatusPopoverLink')}
                i18nStatusErrorPopoverTitle={t('connectionStatusPopoverTitle')}
                icon={<EntityIcon entity={c} alt={c.name} width={23} />}
                loading={isDvConnectionLoading(c)}
                refreshConnectionSchema={handleRefreshSchema}
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
