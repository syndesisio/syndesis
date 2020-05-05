import { useApiResource } from './useApiResource';


export const useDVRoles = () => {
  const { read, ...rest } = useApiResource<string[]>({
    defaultValue: [],
    url: '/status/roles',
    useDvApiUrl: true,
  });


  return {
    ...rest,
    read,
  };
};
