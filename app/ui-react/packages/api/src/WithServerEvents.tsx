import * as React from 'react';
import { callFetch, IFetchHeaders } from './callFetch';

export const EVENT_SERVICE_CONNECTED = 'connected';

export interface IChangeEvent {
  action: string;
  kind: string;
  id: string;
}

export interface IMessageEvent {
  id: string;
  data: string;
  event: string;
}

export interface IWithEventsProps {
  apiUri: string;
  headers: IFetchHeaders;
  children(props: IWithServerEventsChildrenProps): any;
}

export interface IWithServerEventsChildrenProps {
  registerChangeListener: (listener: (event: IChangeEvent) => void) => void;
  registerMessageListener: (listener: (event: IMessageEvent) => void) => void;
  unregisterChangeListener: (listener: (event: IChangeEvent) => void) => void;
  unregisterMessageListener: (listener: (event: IMessageEvent) => void) => void;
}

const RECONNECT_TIME = 5000;

export class WithServerEvents extends React.Component<IWithEventsProps> {
  private starting = false;
  private started = false;
  private unmounting = false;
  private retries = 0;
  private preferredProtocol: string = '';
  private eventSource: EventSource | undefined;
  private webSocket: WebSocket | undefined;
  private changeListeners: Array<(event: IChangeEvent) => void>;
  private messageListeners: Array<(event: IMessageEvent) => void>;

  public constructor(props: IWithEventsProps) {
    super(props);
    this.changeListeners = [];
    this.messageListeners = [];
    this.registerChangeListener = this.registerChangeListener.bind(this);
    this.registerMessageListener = this.registerMessageListener.bind(this);
    this.unregisterChangeListener = this.unregisterChangeListener.bind(this);
    this.unregisterMessageListener = this.unregisterMessageListener.bind(this);
  }

  public registerChangeListener(listener: (event: IChangeEvent) => void) {
    this.changeListeners = [...this.changeListeners, listener];
  }

  public registerMessageListener(listener: (event: IMessageEvent) => void) {
    this.messageListeners = [...this.messageListeners, listener];
  }

  public unregisterChangeListener(listener: (event: IChangeEvent) => void) {
    this.changeListeners = this.changeListeners.filter(l => l !== listener);
  }

  public unregisterMessageListener(listener: (event: IMessageEvent) => void) {
    this.messageListeners = this.messageListeners.filter(l => l !== listener);
  }

  public async componentDidMount() {
    this.start();
  }

  public async componentWillUnmount() {
    this.unmounting = true;
    this.close();
    this.changeListeners = [];
    this.messageListeners = [];
  }

  public shouldComponentUpdate(
    nextProps: Readonly<IWithEventsProps>,
    nextState: Readonly<{}>,
    nextContext: any
  ): boolean {
    return false;
  }

  public render() {
    return this.props.children({
      registerChangeListener: this.registerChangeListener,
      registerMessageListener: this.registerMessageListener,
      unregisterChangeListener: this.unregisterChangeListener,
      unregisterMessageListener: this.unregisterMessageListener,
    });
  }

  private close() {
    this.started = false;
    this.starting = false;
    if (this.webSocket) {
      this.webSocket.close();
      this.webSocket = undefined;
    }
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = undefined;
    }
  }

  private start() {
    this.startConnection(this.retries % 2 === 0);
  }

  private onFailure(error: any) {
    this.close();
    if (this.unmounting) {
      return;
    }
    this.retries++;
    // Initialy retry very quickly.
    let reconnectIn = RECONNECT_TIME;
    if (this.retries < 3) {
      reconnectIn = 1;
    }
    setTimeout(() => {
      // console.log('Reconnecting');
      switch (this.preferredProtocol) {
        // Once we find a protocol that works, keep using it.
        case 'ws':
          this.startConnection(true);
          break;
        case 'es':
          this.startConnection(false);
          break;
        default:
          // Keep flipping between WS and ES untill we find one that works.
          this.startConnection(this.retries % 2 === 0);
          break;
      }
    }, reconnectIn);
  }

  private async startConnection(connectUsingWebSockets: boolean) {
    if (this.starting || this.started || this.unmounting) {
      return;
    }
    this.starting = true;
    try {
      const response = await callFetch({
        body: '',
        headers: this.props.headers,
        method: 'POST',
        url: `${this.props.apiUri}/event/reservations`,
      });
      let reservation = await response.json();
      reservation = reservation.data;
      if (connectUsingWebSockets) {
        let wsApiEndpoint = this.props.apiUri.replace(/^http/, 'ws');
        wsApiEndpoint += '/event/streams.ws/' + reservation;
        // console.log('Connecting using web socket');
        this.starting = false;
        this.started = true;
        this.connectWebSocket(wsApiEndpoint);
      } else {
        this.starting = false;
        this.started = true;
        // console.log('Connecting using server side events');
        this.connectEventSource(
          this.props.apiUri + '/event/streams/' + reservation
        );
      }
    } catch (error) {
      this.onFailure(error);
    }
  }

  private postMessageEvent(messageEvent: IMessageEvent) {
    this.messageListeners.forEach(listener => {
      listener(messageEvent);
    });
  }

  private postChangeEvent(changeEvent: IChangeEvent) {
    this.changeListeners.forEach(listener => {
      listener(changeEvent);
    });
  }

  private connectEventSource(url: string) {
    this.eventSource = new EventSource(url);
    this.eventSource.addEventListener('message', (event: any) => {
      this.started = true;
      this.starting = false;
      this.preferredProtocol = 'es';
      // console.log('sse.message: ', event.data);
      this.postMessageEvent(event);
    });
    this.eventSource.addEventListener('change-event', (event: any) => {
      this.started = true;
      const value = JSON.parse(event.data) as IChangeEvent;
      // console.log('sse.change-event: ', value);
      this.postChangeEvent(value);
    });
    const onError = (event: any) => {
      // console.log('sse.close: ', event);
      this.onFailure(event);
    };
    this.eventSource.addEventListener('close', onError);
    this.eventSource.addEventListener('error', onError);
    return this.eventSource;
  }

  private connectWebSocket(url: string) {
    this.webSocket = new WebSocket(url);
    this.webSocket.onmessage = event => {
      this.started = true;
      this.starting = false;
      const messageEvent = JSON.parse(event.data) as IMessageEvent;
      switch (messageEvent.event) {
        case 'message':
          this.preferredProtocol = 'ws';
          // console.log('ws.message: ', messageEvent.data);
          this.postMessageEvent(messageEvent);
          break;
        case 'change-event':
          const value = JSON.parse(messageEvent.data) as IChangeEvent;
          // console.log('ws.change-event: ', value);
          this.postChangeEvent(value);
          break;
        default:
        // I guess we don't care what happens here...
        // console.log('ws.unknown-message: ', event);
      }
    };
    this.webSocket.onclose = event => {
      // console.log('ws.onclose: ', event);
      this.onFailure(event);
    };
    return this.webSocket;
  }
}
