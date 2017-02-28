// Typings reference file, you can add your own global typings here
// https://www.typescriptlang.org/docs/handbook/writing-declaration-files.html

declare var jQuery: any;
declare var $: any;

declare interface EventSourceInit {
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
