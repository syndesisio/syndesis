export function localStorageFactory(): Storage {
  return (typeof window !== 'undefined') ? window.localStorage : null;
}
