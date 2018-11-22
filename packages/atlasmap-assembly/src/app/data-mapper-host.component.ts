import { Component, ViewChild, OnInit, OnDestroy, Input } from '@angular/core';
import {
  ConfigModel,
  DataMapperAppComponent,
  DocumentInitializationModel,
  DocumentType,
  DocumentManagementService,
  ErrorHandlerService,
  InitializationService,
  InspectionType,
  MappingManagementService,
  MappingDefinition,
} from '@atlasmap/atlasmap-data-mapper';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-data-mapper-host',
  template: '<data-mapper #dataMapperComponent></data-mapper>',
  providers: [
    MappingManagementService,
    ErrorHandlerService,
    DocumentManagementService,
  ],
})
export class DataMapperHostComponent implements OnInit, OnDestroy {
  @Input() inputId: string;
  @Input() inputName: string;
  @Input() inputDescription: string;
  @Input() inputDocumentType: DocumentType;
  @Input() inputInspectionType: InspectionType;
  @Input() inputDataShape: string;
  @Input() outputId: string;
  @Input() outputName: string;
  @Input() outputDescription: string;
  @Input() outputDocumentType: DocumentType;
  @Input() outputInspectionType: InspectionType;
  @Input() outputDataShape: string;
  @ViewChild('dataMapperComponent')
  dataMapperComponent: DataMapperAppComponent;

  private saveMappingSubscription: Subscription;

  constructor(private initializationService: InitializationService) {}

  ngOnInit() {
    // initialize config information before initializing services
    const c: ConfigModel = this.initializationService.cfg;

    // initialize base urls for our service calls
    c.initCfg.baseJavaInspectionServiceUrl = '/api/v1/atlas/java/';
    c.initCfg.baseXMLInspectionServiceUrl = '/api/v1/atlas/xml/';
    c.initCfg.baseJSONInspectionServiceUrl = '/api/v1/atlas/json/';
    c.initCfg.baseMappingServiceUrl = '/api/v1/atlas/';

    // // enable the navigation bar and import/export for stand-alone
    c.initCfg.disableNavbar = false;
    //
    c.initCfg.disableMappingPreviewMode = false;
    c.initCfg.discardNonMockSources = false;
    c.initCfg.addMockJSONMappings = false;
    c.initCfg.addMockJavaSingleSource = false;
    c.initCfg.addMockJavaSources = false;
    c.initCfg.addMockXMLInstanceSources = false;
    c.initCfg.addMockXMLSchemaSources = false;
    c.initCfg.addMockJSONSources = false;
    c.initCfg.addMockJavaTarget = false;
    c.initCfg.addMockXMLInstanceTarget = false;
    c.initCfg.addMockXMLSchemaTarget = false;
    c.initCfg.addMockJSONTarget = false;
    c.initCfg.debugDocumentServiceCalls = false;
    c.initCfg.debugMappingServiceCalls = false;
    c.initCfg.debugClassPathServiceCalls = false;
    c.initCfg.debugValidationServiceCalls = false;
    c.initCfg.debugFieldActionServiceCalls = false;
    c.initCfg.debugDocumentParsing = false;

    // enable debug logging options as needed
    c.initCfg.debugDocumentServiceCalls = true;
    c.initCfg.debugDocumentParsing = false;
    c.initCfg.debugMappingServiceCalls = false;
    c.initCfg.debugClassPathServiceCalls = false;
    c.initCfg.debugValidationServiceCalls = false;
    c.initCfg.debugFieldActionServiceCalls = false;

    const inputDoc: DocumentInitializationModel = new DocumentInitializationModel();
    inputDoc.type = this.inputDocumentType;
    inputDoc.inspectionType = this.inputInspectionType;
    inputDoc.inspectionSource = this.inputDataShape;
    inputDoc.id = this.inputId;
    inputDoc.name = this.inputName;
    inputDoc.description = this.inputDescription;
    inputDoc.isSource = true;
    inputDoc.showFields = true;
    c.addDocument(inputDoc);

    const outputDoc: DocumentInitializationModel = new DocumentInitializationModel();
    outputDoc.type = this.outputDocumentType;
    outputDoc.inspectionType = this.outputInspectionType;
    outputDoc.inspectionSource = this.outputDataShape;
    outputDoc.id = this.outputId;
    outputDoc.name = this.outputName;
    outputDoc.description = this.outputDescription;
    outputDoc.isSource = false;
    outputDoc.showFields = true;
    c.addDocument(outputDoc);

    c.mappings = new MappingDefinition();

    this.initializationService.initialize();
  }

  ngOnDestroy() {
    this.saveMappingSubscription.unsubscribe();
  }
}
