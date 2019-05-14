/**
 * Simple debounce function that will only execute the given function
 * after waiting the specified time period
 *
 * @param func
 * @param wait
 * @param immediate
 */
export function debounce(func: () => void, wait: number, immediate: boolean) {
  let timeout: number | null;
  return () => {
    const later = () => {
      timeout = null;
      if (!immediate) {
        func();
      }
    };
    const callNow = immediate && typeof timeout !== 'undefined';
    if (typeof timeout !== 'undefined') {
      window.clearTimeout(timeout!);
    }
    timeout = window.setTimeout(later, wait);
    if (callNow) {
      func();
    }
  };
}
