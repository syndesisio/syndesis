import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { resolve } from 'url';

import { ConfigService } from '@syndesis/ui/config.service';
import { log } from '@syndesis/ui/logging';
import { ApiHttpService } from '@syndesis/ui/platform';

export class ChangeEvent {
  action: string;
  kind: string;
  id: string;
}
class MessageEvent {
  id: string;
  data: string;
  event: string;
}

const RECONNECT_TIME = 5000;

@Injectable()
export class EventsService {
  messageEvents = new Subject<String>();
  changeEvents = new Subject<ChangeEvent>();

  private eventSource: EventSource;
  private webSocket: WebSocket;
  private starting = false;
  private started = false;
  private retries = 0;
  private preferredProtocol = null;

  constructor(
    private configService: ConfigService,
    private apiHttpService: ApiHttpService
  ) {
    this.configService.asyncSettings$.subscribe(() => this.start());
  }

  start() {
    if (!this.started) {
      this.startConnection(this.retries % 2 === 0);
    }
  }

  onFailure(event) {
    this.started = false;
    this.starting = false;
    this.retries++;

    if (this.webSocket) {
      this.webSocket.close();
      this.webSocket = undefined;
    }
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = undefined;
    }

    // Initialy retry very quickly.
    let reconnectIn = RECONNECT_TIME;
    if (this.retries < 3) {
      reconnectIn = 1;
    }

    setTimeout(() => {
      log.info('Reconnecting');
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

  private startConnection(connectUsingWebSockets) {
    if (this.starting) {
      return;
    }
    this.starting = true;

    try {
      this.apiHttpService
        .setEndpointUrl('/event/reservations')
        .post<{ data: any }>({})
        .subscribe(
          response => {
            const apiEndpoint = this.configService.getSettings().apiEndpoint;
            const reservation = response.data;

            try {
              if (connectUsingWebSockets) {
                let wsApiEndpoint = resolve(window.location.href, apiEndpoint);
                wsApiEndpoint = wsApiEndpoint.replace(/^http/, 'ws');
                (wsApiEndpoint += '/event/streams.ws/' + reservation),
                  this.connectWebSocket(wsApiEndpoint);
                log.info('Connecting using web socket');
                this.starting = false;
                this.started = true;
              } else {
                this.connectEventSource(
                  apiEndpoint + '/event/streams/' + reservation
                );
                this.starting = false;
                this.started = true;
                log.info('Connecting using server side events');
              }
            } catch (error) {
              this.onFailure(error);
            }
          },
          error => this.onFailure(error)
        );
    } catch (error) {
      this.onFailure(error);
    }
  }

  private connectEventSource(url: string) {
    this.eventSource = new EventSource(url);
    this.eventSource.addEventListener('message', (event: any) => {
      this.started = true;
      this.starting = false;
      this.preferredProtocol = 'es';
      log.info('sse.message: ' + JSON.stringify(event.data));
      this.messageEvents.next(event.data);
    });
    this.eventSource.addEventListener('change-event', (event: any) => {
      this.started = true;
      const value = JSON.parse(event.data) as ChangeEvent;
      log.info('sse.change-event: ' + JSON.stringify(value));
      this.changeEvents.next(value);
    });
    const onError = event => {
      log.info('sse.close: ' + JSON.stringify(event));
      this.onFailure(event);
    };
    this.eventSource.addEventListener('close', onError);
    this.eventSource.addEventListener('error', onError);
  }

  private connectWebSocket(url) {
    this.webSocket = new WebSocket(url);
    this.webSocket.onmessage = event => {
      this.started = true;
      const messageEvent = JSON.parse(event.data) as MessageEvent;
      switch (messageEvent.event) {
        case 'message':
          this.starting = false;
          this.preferredProtocol = 'ws';
          log.info('ws.message: ' + JSON.stringify(messageEvent.data));
          this.messageEvents.next(messageEvent.data);
          break;
        case 'change-event':
          log.info('ws.change-event: ' + JSON.stringify(messageEvent.data));
          const value = JSON.parse(messageEvent.data) as ChangeEvent;
          this.changeEvents.next(value);
          break;
        default:
          log.info('ws.unknown-message: ' + JSON.stringify(event));
      }
    };
    this.webSocket.onclose = event => {
      log.info('ws.onclose: ' + JSON.stringify(event));
      this.onFailure(event);
    };
  }
}
