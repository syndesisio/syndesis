import { IConnectionOverview } from '@syndesis/models';
import { getActionsWithPattern, isConfigRequired, isDerived } from './helpers';
import { useApiResource } from './useApiResource';
import { transformConnectorResponse } from './useConnector';
import { useServerEvents } from './useServerEvents';
import { IChangeEvent } from './WithServerEvents';

export const transformConnectionResponse = (
  connection: IConnectionOverview
) => {
  const connector = connection.connector
    ? transformConnectorResponse(connection.connector)
    : undefined;
  return {
    ...connection,
    actionsWithFrom: getActionsWithPattern(
      'From',
      connector ? connector.actions : []
    ),
    actionsWithPipe: getActionsWithPattern(
      'Pipe',
      connector ? connector.actions : []
    ),
    actionsWithPollEnrich: getActionsWithPattern(
      'PollEnrich',
      connector ? connector.actions : []
    ),
    actionsWithTo: getActionsWithPattern(
      'To',
      connector ? connector.actions : []
    ),
    connector,
    derived: isDerived(connection),
    isConfigRequired: isConfigRequired(connection),
    isTechPreview: connector ? connector.isTechPreview : false,
  };
};

export const useConnection = (
  connectionId: string,
  initialValue?: IConnectionOverview,
  disableUpdates: boolean = false
) => {
  const { read, ...rest } = useApiResource<IConnectionOverview>({
    defaultValue: {
      isConfigRequired: false,
      isTechPreview: false,
      name: '',
    },
    initialValue,
    transformResponse: async response => {
      const connection = await response.json();
      return transformConnectionResponse(connection);
    },
    url: `/connections/${connectionId}`,
  });
  useServerEvents({
    disableUpdates,
    filter: (change: IChangeEvent) => change.id === connectionId,
    read,
  });
  return {
    ...rest,
    read,
  };
};
