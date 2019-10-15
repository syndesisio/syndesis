import { useEffect, useRef } from 'react';

export function usePrevious(value: any) {
  const ref = useRef();

  // Store current value in ref
  useEffect(() => {
    ref.current = value;
  }, [value]); // Re-run when value changes

  // Happens before useEffect above
  return ref.current;
}
