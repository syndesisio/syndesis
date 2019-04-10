import {
  Component,
  NgZone,
  OnDestroy,
  OnInit,
  ViewEncapsulation,
} from '@angular/core';
import {
  DocumentManagementService,
  DocumentType,
  ErrorHandlerService,
  InitializationService,
  InspectionType,
  MappingManagementService,
} from '@atlasmap/atlasmap-data-mapper';

export interface IInitMessagePayload {
  documentId: string;
  inputName: string;
  inputDescription: string;
  inputDocumentType: DocumentType;
  inputInspectionType: InspectionType;
  inputDataShape: string;
  outputName: string;
  outputDescription: string;
  outputDocumentType: DocumentType;
  outputInspectionType: InspectionType;
  outputDataShape: string;
  mappings?: string;
}

export interface IMappingsMessagePayload {
  mappings: string;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [
    InitializationService,
    MappingManagementService,
    ErrorHandlerService,
    DocumentManagementService,
  ],
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'atlasmap';

  loading = true;
  documentId: string;
  inputName: string;
  inputDescription: string;
  inputDocumentType: DocumentType;
  inputInspectionType: InspectionType;
  inputDataShape: string;
  outputName: string;
  outputDescription: string;
  outputDocumentType: DocumentType;
  outputInspectionType: InspectionType;
  outputDataShape: string;
  mappings?: string;

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

  onUpdateMessage(payload: IInitMessagePayload) {
    this.loading = false;
    this.documentId = payload.documentId;
    this.inputName = payload.inputName;
    this.inputDescription = payload.inputDescription;
    this.inputDocumentType = payload.inputDocumentType;
    this.inputInspectionType = payload.inputInspectionType;
    this.inputDataShape = payload.inputDataShape;
    this.outputName = payload.outputName;
    this.outputDescription = payload.outputDescription;
    this.outputDocumentType = payload.outputDocumentType;
    this.outputInspectionType = payload.outputInspectionType;
    this.outputDataShape = payload.outputDataShape;
    this.mappings = payload.mappings;
  }

  onMappings(mappings: string) {
    if (this.messagePort) {
      this.messagePort.postMessage({
        message: 'mappings',
        payload: {
          mappings,
        } as IMappingsMessagePayload,
      });
    }
  }
}
