import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import {
  InitializationService,
  ErrorHandlerService,
  DocumentManagementService,
  MappingManagementService,
  DocumentType,
  InspectionType,
} from '@atlasmap/atlasmap-data-mapper';

interface IInitMessagePayload {
  inputId: string;
  inputName: string;
  inputDescription: string;
  inputDocumentType: DocumentType;
  inputInspectionType: InspectionType;
  inputDataShape: string;
  outputId: string;
  outputName: string;
  outputDescription: string;
  outputDocumentType: DocumentType;
  outputInspectionType: InspectionType;
  outputDataShape: string;
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
  inputId: string;
  inputName: string;
  inputDescription: string;
  inputDocumentType: DocumentType;
  inputInspectionType: InspectionType;
  inputDataShape: string;
  outputId: string;
  outputName: string;
  outputDescription: string;
  outputDocumentType: DocumentType;
  outputInspectionType: InspectionType;
  outputDataShape: string;

  ngOnInit(): void {
    this.onMessage = this.onMessage.bind(this);
    this.onInitMessage = this.onInitMessage.bind(this);

    window.addEventListener('message', this.onMessage);
  }

  ngOnDestroy(): void {
    window.removeEventListener('message', this.onMessage);
  }

  onMessage(event: MessageEvent) {
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
    this.inputId = payload.inputId;
    this.inputName = payload.inputName;
    this.inputDescription = payload.inputDescription;
    this.inputDocumentType = payload.inputDocumentType;
    this.inputInspectionType = payload.inputInspectionType;
    this.inputDataShape = payload.inputDataShape;
    this.outputId = payload.outputId;
    this.outputName = payload.outputName;
    this.outputDescription = payload.outputDescription;
    this.outputDocumentType = payload.outputDocumentType;
    this.outputInspectionType = payload.outputInspectionType;
    this.outputDataShape = payload.outputDataShape;
  }
}
