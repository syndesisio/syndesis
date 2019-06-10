import { useApiResource } from './useApiResource';

export interface ICredentialsConnectResponse {
  redirectUrl: string;
  type: string;
  state: {
    persist: string;
    spec: string;
  };
}

export const useConnectorCredentialsConnect = () => {
  const { read, ...rest } = useApiResource<
    ICredentialsConnectResponse | undefined
  >({
    defaultValue: undefined,
    method: 'GET',
    readOnMount: false,
    url: '',
  });
  return {
    ...rest,
    read: (connectorId: string, returnUrl: string) =>
      read({
        body: { returnUrl },
        method: 'POST',
        url: `/connectors/${connectorId}/credentials`,
      }),
  };
};
