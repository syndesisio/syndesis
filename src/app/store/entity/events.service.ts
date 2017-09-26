import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { Subscription } from 'rxjs/Subscription';
import { ConfigService } from '../../config.service';
import { Restangular } from 'ngx-restangular';
import { log } from '../../logging';

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

const MAX_RETRIES = 5;
const RECONNECT_TIME = 5000;

@Injectable()
export class EventsService {
  private eventSource: EventSource;
  private webSocket: WebSocket;
  private starting = false;

  messageEvents: Subject<String> = new Subject<String>();
  changeEvents: Subject<ChangeEvent> = new Subject<ChangeEvent>();

  constructor(private config: ConfigService, private restangular: Restangular) {
    this.startConnection();
  }

  private startConnection() {
    if (this.starting) {
      return;
    }
    this.starting = true;
    // Setup an event stream reservation first..
    const createReservation = (retries = 0) => {
      let sub: Subscription = undefined;
      try {
        sub = this.restangular.all('event/reservations').customPOST().subscribe(
          response => {
            const apiEndpoint = this.config.getSettings().apiEndpoint;
            const reservation = response.data;
            try {
              // First try to connect via a WebSocket
              const wsApiEndpoint = apiEndpoint.replace(/^http/, 'ws');
              this.connectWebSocket(
                wsApiEndpoint + '/event/streams.ws/' + reservation,
              );
              log.info('Connecting using web socket');
              this.starting = false;
            } catch (error) {
              // Then fallback to using EventSource
              log.info('Unable to connect web socket, falling back to SSE');
              try {
                this.connectEventSource(
                  apiEndpoint + '/event/streams/' + reservation,
                );
                this.starting = false;
                log.info('Connecting using server side events');
              } catch (err) {
                log.info('Failed to connect to event source');
              }
            }
          },
          error => {
            if (sub) {
              sub.unsubscribe();
            }
            // try and reconnect
            if (this.eventSource) {
              this.eventSource.close();
              this.eventSource = undefined;
            }
            if (this.webSocket) {
              this.webSocket.close();
              this.webSocket = undefined;
            }
            if (retries >= MAX_RETRIES) {
              log.info(
                'Giving up event stream reservation, refresh the page to retry',
              );
              return;
            }
            setTimeout(() => {
              log.info('Attempting to fetch new event stream reservation');
              createReservation(retries + 1);
            }, RECONNECT_TIME);
          },
        );
      } catch (err) {
        if (sub) {
          sub.unsubscribe();
        }
        if (retries >= MAX_RETRIES) {
          log.info(
            'Giving up event stream reservation, refresh the page to retry',
          );
          return;
        }
        setTimeout(() => {
          log.info('Attempting to fetch new event stream reservation');
          createReservation(retries + 1);
        }, RECONNECT_TIME);
      }
    };
    createReservation();
  }

  private connectEventSource(url: string) {
    const setupEventSource = (eventSource: EventSource, retries = 0) => {
      const onMessage = event => {
        log.info('sse.message: ' + JSON.stringify(event.data));
        this.messageEvents.next(event.data);
      };
      const onChangeEvent = event => {
        const value = JSON.parse(event.data) as ChangeEvent;
        log.info('sse.change-event: ' + JSON.stringify(value));
        this.changeEvents.next(value);
      };
      const onClose = event => {
        log.info('sse.close: ' + JSON.stringify(event));
        if (retries >= MAX_RETRIES) {
          log.info(
            'ss.close: Max retries reached, giving up trying to reconnect SSE',
          );
          return;
        }
        setTimeout(() => {
          log.info('sse.close: attempting to reconnect');
          this.eventSource = setupEventSource(
            new EventSource(url),
            retries + 1,
          );
        }, RECONNECT_TIME);
      };
      const onError = event => {
        log.info('sse.error: attempting to reconnect');
        try {
          eventSource.removeEventListener('message', onMessage);
          eventSource.removeEventListener('change-event', onChangeEvent);
          eventSource.removeEventListener('close', onClose);
          eventSource.removeEventListener('error', onError);
          eventSource.close();
        } catch (err) {
          // ignore
        }
        // This happens frequently, so let's reconnect sooner
        setTimeout(() => {
          this.startConnection();
        }, 1000);
      };
      eventSource.addEventListener('message', onMessage);
      eventSource.addEventListener('change-event', onChangeEvent);
      eventSource.addEventListener('close', onClose);
      eventSource.addEventListener('error', onError);
      return eventSource;
    };
    this.eventSource = setupEventSource(new EventSource(url));
  }

  private connectWebSocket(url) {
    const setupWebSocket = (ws, retries = 0) => {
      const onMessage = event => {
        const messageEvent = JSON.parse(event.data) as MessageEvent;
        switch (messageEvent.event) {
          case 'message':
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
      const onClose = event => {
        log.info('ws.onclose: ' + JSON.stringify(event));
        if (retries >= MAX_RETRIES) {
          log.info(
            'ws.onclose: Max retries reached, giving up trying to reconnect websocket',
          );
          return;
        }
        setTimeout(() => {
          log.info('ws.onclose: attempting to reconnect');
          try {
            this.webSocket = setupWebSocket(new WebSocket(url), retries + 1);
          } catch (err) {
            log.info('ws connect failed, getting new registration');
            this.webSocket = undefined;
            this.startConnection();
          }
        }, RECONNECT_TIME);
      };
      ws.onmessage = onMessage;
      ws.onclose = onClose;
      return ws;
    };
    this.webSocket = setupWebSocket(new WebSocket(url));
  }
}
