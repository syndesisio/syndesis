import { debounce } from '@syndesis/utils';
import { useCallback, useContext, useEffect } from 'react';
import { ServerEventsContext } from './ServerEventsContext';
import {
  EVENT_SERVICE_CONNECTED,
  IChangeEvent,
  IMessageEvent,
} from './WithServerEvents';

export interface IUseServerEventsProps {
  filter: (change: IChangeEvent) => boolean;
  read: () => void;
  disableUpdates?: boolean;
  disableDebounce?: boolean;
  debounceWait?: number;
}

export function useServerEvents({
  filter,
  read,
  disableUpdates,
  disableDebounce,
  debounceWait = 250,
}: IUseServerEventsProps) {
  const {
    registerChangeListener,
    registerMessageListener,
    unregisterChangeListener,
    unregisterMessageListener,
  } = useContext(ServerEventsContext);

  const debouncedRead = useCallback(
    disableDebounce ? read : debounce(read, debounceWait, false),
    [disableDebounce, debounceWait, read]
  );

  const messageListener = useCallback(
    (event: IMessageEvent) => {
      if (event.data === EVENT_SERVICE_CONNECTED) {
        debouncedRead();
      }
    },
    [debouncedRead]
  );

  const changeListener = useCallback(
    (event: IChangeEvent) => {
      if (filter(event)) {
        debouncedRead();
      }
    },
    [filter, debouncedRead]
  );

  useEffect(
    function registerListeners() {
      if (!disableUpdates) {
        registerChangeListener(changeListener);
        registerMessageListener(messageListener);

        return function unregisterListeners() {
          unregisterChangeListener(changeListener);
          unregisterMessageListener(messageListener);
        };
      }
      return undefined;
    },
    [
      registerChangeListener,
      registerMessageListener,
      unregisterChangeListener,
      unregisterMessageListener,
      changeListener,
      messageListener,
      disableUpdates,
    ]
  );
}
