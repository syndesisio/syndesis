import { Virtualization } from '@syndesis/models';
import * as React from 'react';
import isEqual from 'react-fast-compare';
import { useApiResource } from './useApiResource';
import { usePolling } from './usePolling';

/**
 * @property {Virtualization} model updates only when the value of `resource` changes
 * @property {Virtualization} resource updates each time fetch is called
 * @param virtualizationName the name of the virtualization being requested
 * @param disableUpdates `true` if polling should be enabled (defaults to `false`)
 */
export const useVirtualization = (
  virtualizationName: string,
  disableUpdates: boolean = false
) => {
  const { read, resource, ...rest } = useApiResource<Virtualization>({
    defaultValue: {
      deployedState: 'NOTFOUND',
      description: '',
      empty: true,
      id: '',
      modified: true,
      name: '',
      publishedState: 'NOTFOUND',
      secured: false,
      serviceViewModel: '',
      usedBy: []
    },
    url: `virtualizations/${virtualizationName}`,
    useDvApiUrl: true,
  });

  /**
   * Update `model` only if `resource` changes.
   */
  const [model, setModel] = React.useState(resource);
  React.useEffect(() => {
    if (!isEqual(resource, model)) {
      setModel(resource);
    }
  }, [resource, model, setModel]);

  // only poll if updates are turned on
  const poller = React.useCallback(() => {
    // tslint:disable-next-line: no-unused-expression
     disableUpdates ? () => void 0 : read();
  }, []);
  usePolling({ callback: poller, delay: 5000 });

  return {
    ...rest,
    model, // updates only when the resource changes
    read,
    resource, // updates each time polling runs
  };
};
