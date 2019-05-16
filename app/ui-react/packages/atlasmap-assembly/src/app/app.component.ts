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

export interface IDocumentProps {
  id: string;
  name: string;
  description: string;
  documentType: DocumentType;
  inspectionType: InspectionType;
  inspectionSource: string;
  inspectionResult: string;
  showFields: boolean;
}

export interface IInitMessagePayload {
  documentId: string;
  inputDocuments: IDocumentProps[];
  outputDocument: IDocumentProps;
  initialMappings?: string;
  baseJavaInspectionServiceUrl: string;
  baseXMLInspectionServiceUrl: string;
  baseJSONInspectionServiceUrl: string;
  baseMappingServiceUrl: string;
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
  inputDocuments: IDocumentProps[];
  outputDocument: IDocumentProps;
  initialMappings?: string;
  baseJavaInspectionServiceUrl: string;
  baseXMLInspectionServiceUrl: string;
  baseJSONInspectionServiceUrl: string;
  baseMappingServiceUrl: string;

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
    this.inputDocuments = payload.inputDocuments;
    this.outputDocument = payload.outputDocument;
    this.initialMappings = payload.initialMappings;
    this.baseJavaInspectionServiceUrl = payload.baseJavaInspectionServiceUrl;
    this.baseXMLInspectionServiceUrl = payload.baseXMLInspectionServiceUrl;
    this.baseJSONInspectionServiceUrl = payload.baseJSONInspectionServiceUrl;
    this.baseMappingServiceUrl = payload.baseMappingServiceUrl;
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
