import { DVStatus } from '@syndesis/models';
import { useApiResource } from './useApiResource';

const dvStatus: DVStatus = {
    attributes: {
      exposeVia3scale: "false",
      ssoConfigured: "false"
    }
  }

export const useDVStatus = () => {
  const { read, ...rest } = useApiResource<DVStatus>({
    defaultValue: dvStatus,
    url: '/status',
    useDvApiUrl: true,
  });


  return {
    ...rest,
    read,
  };
};
