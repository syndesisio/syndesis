import { useEffect, useRef } from 'react';

export interface IUsePollingProps {
  delay: number;
  callback: () => void;
}

export function usePolling({ delay, callback }: IUsePollingProps) {
  const savedCallback = useRef<() => void>(callback);

  // Remember the latest callback.
  useEffect(() => {
    savedCallback.current = callback;
  }, [callback]);

  // Set up the interval.
  useEffect(() => {
    function tick() {
      savedCallback.current();
    }

    if (delay !== null) {
      const id = setInterval(tick, delay);
      return () => clearInterval(id);
    }

    return undefined;
  }, [delay]);
}
