import { AcquisitionMethod } from '@syndesis/models';
import { useApiResource } from './useApiResource';

export const useConnectorCredentials = (connectorId: string) => {
  return useApiResource<AcquisitionMethod | undefined>({
    defaultValue: {},
    transformResponse: am => (Object.keys(am).length > 0 ? am : undefined),
    url: `/connectors/${connectorId}/credentials`,
  });
};

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
    url: `/connectors/${connectorId}/credentials`,
  });
};
