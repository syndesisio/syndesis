declare module 'workbox-window' {
  class Workbox {
    constructor(scriptURL: string, registerOptions?: object);

    register(immediate?: boolean): Promise<any>;
    active(): Promise<ServiceWorker>;
    controlling(): Promise<ServiceWorker>;
    getSW(): Promise<ServiceWorker>;
    messageSW(data: object): Promise<object>;

    addEventListener(event: 'message', callback: (data: IWorkboxEventMessage) => void): void;
    addEventListener(event: 'installed', callback: (data: IWorkboxEvent) => void): void;
    addEventListener(event: 'waiting', callback: (data: IWorkboxEventWaiting) => void): void;
    addEventListener(event: 'controlling', callback: (data: IWorkboxEvent) => void): void;
    addEventListener(event: 'activated', callback: (data: IWorkboxEvent) => void): void;
    addEventListener(event: 'redundant', callback: (data: IWorkboxEvent) => void): void;
    addEventListener(event: 'externalinstalled', callback: (data: IWorkboxEventExternal) => void): void;
    addEventListener(event: 'externalwaiting', callback: (data: IWorkboxEventExternal) => void): void;
    addEventListener(event: 'externalactivated', callback: (data: IWorkboxEventExternal) => void): void;
  }

  type WorkboxEvent = 'message' | 'installed' | 'waiting' | 'controlling' | 'activated' | 'redundant' | 'externalinstalled' | 'externalwaiting' | 'externalactivated';

  interface IWorkboxEventBase {
    originalEvent: Event;
    type: WorkboxEvent;
    target: Workbox;
  }

  interface IWorkboxEventMessage extends IWorkboxEventBase {
    data: any;
  }

  interface IWorkboxEvent extends IWorkboxEventBase {
    sw: ServiceWorker;
    isUpdate: boolean|undefined;
  }

  interface IWorkboxEventWaiting extends IWorkboxEvent {
    wasWaitingBeforeRegister: boolean|undefined;
  }

  interface IWorkboxEventExternal extends IWorkboxEventBase {
    sw: ServiceWorker;
  }
}
