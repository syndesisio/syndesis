import * as React from 'react';

export interface IUsePollingProps {
  polling: number;
  read: () => void;
}

export function usePolling({ polling, read }: IUsePollingProps) {
  const [pollingTimer, setPollingTimer] = React.useState();

  const poller = () => {
    read();
  };

  const stopPolling = () => {
    if (pollingTimer) {
      clearInterval(pollingTimer);
      setPollingTimer(undefined);
    }
  };

  const startPolling = () => {
    if (!pollingTimer && polling > 0) {
      setPollingTimer(setInterval(poller, polling));
    }
  };

  React.useEffect(() => {
    // mount
    startPolling();

    // unmount
    return () => {
      stopPolling();
    };
  });
}
