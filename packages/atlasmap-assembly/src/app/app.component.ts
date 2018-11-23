import { Component, OnDestroy, OnInit } from '@angular/core';
import {
  InitializationService,
  ErrorHandlerService,
  DocumentManagementService,
  MappingManagementService,
  DocumentType,
  InspectionType,
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
  providers: [
    InitializationService,
    MappingManagementService,
    ErrorHandlerService,
    DocumentManagementService,
  ],
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'atlasmap';

  hasData = false;
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

  ngOnInit(): void {
    this.onMessage = this.onMessage.bind(this);
    this.onInitMessage = this.onInitMessage.bind(this);

    window.addEventListener('message', this.onMessage);
  }

  ngOnDestroy(): void {
    window.removeEventListener('message', this.onMessage);
  }

  onMessage(event: MessageEvent) {
    this.messagePort = event.ports[0];

    switch (event.data.message) {
      case 'init':
        this.onInitMessage(event.data.payload);
        break;
      default:
      // nohop
    }
  }

  onInitMessage(payload: IInitMessagePayload) {
    this.hasData = true;
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
