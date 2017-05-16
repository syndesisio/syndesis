import {Injectable} from '@angular/core';
import {Subject} from 'rxjs/Subject';
import {ConfigService} from '../../config.service';
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

@Injectable()
export class EventsService {

  private eventSource: EventSource;
  private webSocket: WebSocket;

  messageEvents: Subject<String> = new Subject<String>();
  changeEvents: Subject<ChangeEvent> = new Subject<ChangeEvent>();

  constructor(config: ConfigService, restangular: Restangular) {
    // Setup an event stream reservation first..
    restangular.all('event/reservations').customPOST().subscribe( response => {
      const apiEndpoint = config.getSettings().apiEndpoint;
      const reservation = response.data;
      try {
        // First try to connect via a WebSocket
        const wsApiEndpoint = apiEndpoint.replace( /^http/, 'ws' );
        this.connectWebSocket( wsApiEndpoint + '/event/streams.ws/' + reservation  );
      } catch ( error ) {
        // Then fallback to using EventSource
        this.connectEventSource(  apiEndpoint + '/event/streams/' + reservation );
      }
    });
  }

  private connectEventSource(url: string) {
    const eventSource = new EventSource( url);

    eventSource.addEventListener( 'message', (event) => {
      log.info('sse.message: ' + JSON.stringify(event.data));
      this.messageEvents.next( event.data );
    } );
    eventSource.addEventListener( 'change-event', (event) => {
      const value = JSON.parse( event.data ) as ChangeEvent;
      log.info('sse.change-event: ' + JSON.stringify(value));
      this.changeEvents.next( value );
    } );
    eventSource.addEventListener( 'close', (event) => {
      log.info('sse.close: ' + JSON.stringify(event));
      // TODO: reconnect?
    } );
    this.eventSource = eventSource;
  }

  private connectWebSocket(url) {
    const ws = new WebSocket( url );
    ws.onmessage = (event) => {
      const messageEvent = JSON.parse( event.data ) as MessageEvent;
      switch (messageEvent.event) {
        case 'message':
          log.info('ws.message: ' + JSON.stringify(messageEvent.data));
          // console.log('ws.message', messageEvent.data)
          this.messageEvents.next( messageEvent.data );
          break;
        case 'change-event':
          log.info('ws.change-event: ' + JSON.stringify(messageEvent.data));
          const value = JSON.parse( messageEvent.data ) as ChangeEvent;
          //console.log('ws.change-event', value)
          this.changeEvents.next( value );
          break;
        default:
        //console.log('ws.onmessage', event)
      }
    };
    ws.onclose = (event) => {
      log.info('ws.onclose: ' + JSON.stringify(event));
      // TODO: reconnect?
    };
    this.webSocket = ws;
  }

}
