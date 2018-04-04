// Typings reference file, you can add your own global typings here
// https://www.typescriptlang.org/docs/handbook/writing-declaration-files.html

interface EventSourceInit {
  withCredentials?: boolean;
}

declare class EventSource {
  url: string;
  withCredentials: boolean;
  CONNECTING: number;
  OPEN: number;
  CLOSED: number;
  readyState: number;

  onmessage: (event: any) => void;

  close: () => void;

  constructor(url: string, eventSourceInitDict?: EventSourceInit);

  addEventListener(event: string, cb: (event: any) => void);

  removeEventListener(event: string, cb: (event: any) => void);
}

// add type definition for https://github.com/liabru/jquery-match-height plugin
interface JQuery {
  matchHeight(options?: any): any;
}
// Allows for importing JSON values as ES modules in TypeScript
declare module '*.json' {
  const value: any;
  export default value;
}
