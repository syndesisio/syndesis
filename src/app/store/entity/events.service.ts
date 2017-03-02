import {Injectable} from '@angular/core';
import {Subject} from 'rxjs/Subject';
import {ConfigService} from '../../config.service';
import { Restangular } from 'ng2-restangular';

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
      let apiEndpoint = config.getSettings().apiEndpoint;
      const reservation = response.data;
      try {
        // First try to connect via an EventSource (not supported on IE)
        this.connectEventSource(  apiEndpoint + '/event/streams/' + reservation );
      } catch ( error ) {
        // Then fallback to using WebSockets
        apiEndpoint = apiEndpoint.replace( /^http/, 'ws' );
        this.connectWebSocket( apiEndpoint + '/event/streams.ws/' + reservation  );
      }
    });
  }

  private connectEventSource(url: string) {
    const eventSource = new EventSource( url);


    eventSource.addEventListener( 'message', (event) => {
      //console.log('sse.message', event.data)
      this.messageEvents.next( event.data );
    } );
    eventSource.addEventListener( 'change-event', (event) => {
      const value = JSON.parse( event.data ) as ChangeEvent;
      //console.log('sse.change-event', value)
      this.changeEvents.next( value );
    } );
    eventSource.addEventListener( 'close', (event) => {
      //console.log('sse.close', event)
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
          // console.log('ws.message', messageEvent.data)
          this.messageEvents.next( messageEvent.data );
          break;
        case 'change-event':
          const value = JSON.parse( messageEvent.data ) as ChangeEvent;
          //console.log('ws.change-event', value)
          this.changeEvents.next( value );
          break;
        default:
        //console.log('ws.onmessage', event)
      }
    };
    ws.onclose = (event) => {
      //console.log('ws.onclose', event)
      // TODO: reconnect?
    };
    this.webSocket = ws;
  }

}
