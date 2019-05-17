import {
  Component,
  NgZone,
  OnDestroy,
  OnInit,
  ViewEncapsulation,
} from '@angular/core';
import { ApiDefinition as AD } from 'apicurio-design-studio';

// tslint:disable-next-line
export interface ApiDefinition extends AD {}

export interface IInitPayload {
  specification: string;
}

export interface IApiDefinitionPayload {
  specification: ApiDefinition;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'apicurio';
  loading = true;
  specification?: string;
  messagePort?: MessagePort;

  constructor(private _ngZone: NgZone) {}

  ngOnInit(): void {
    this.onMessagePort = this.onMessagePort.bind(this);
    this.onMessages = this.onMessages.bind(this);
    this.onUpdateMessage = this.onUpdateMessage.bind(this);

    window.addEventListener('message', this.onMessagePort);
  }

  ngOnDestroy(): void {
    window.removeEventListener('message', this.onMessagePort);
  }

  onMessagePort(event: MessageEvent) {
    this.messagePort = event.ports[0];
    this.messagePort.onmessage = this.onMessages;

    this.messagePort.postMessage({
      message: 'ready',
    });
  }

  onMessages(event: MessageEvent) {
    this._ngZone.run(() => {
      switch (event.data.message) {
        case 'update':
          this.onUpdateMessage(event.data.payload);
          break;
        default:
        // nohop
      }
    });
  }

  onUpdateMessage(payload: IInitPayload) {
    this.loading = false;
    this.specification = payload.specification;
  }

  onSpecification(specification: ApiDefinition) {
    if (this.messagePort) {
      this.messagePort.postMessage({
        message: 'specification',
        payload: {
          specification,
        } as IApiDefinitionPayload,
      });
    }
  }
}
