import { useApiResource } from './useApiResource';

export interface ICredentialsConnectResponse {
  redirectUrl: string;
  type: string;
  state: {
    persist: string;
    spec: string;
  };
}

export const useConnectorCredentialsConnect = (
  connectorId: string,
  returnUrl: string
) => {
  return useApiResource<ICredentialsConnectResponse | undefined>({
    body: { returnUrl },
    defaultValue: undefined,
    method: 'POST',
    readOnMount: true,
    url: `/connectors/${connectorId}/credentials`,
  });
};
