import {
  Component,
  ViewChild,
  OnInit,
  OnDestroy,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
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
  MappingSerializer,
} from '@atlasmap/atlasmap-data-mapper';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-data-mapper-host',
  template: `
    <data-mapper #dataMapperComponent></data-mapper>
  `,
  providers: [
    MappingManagementService,
    ErrorHandlerService,
    DocumentManagementService,
  ],
})
export class DataMapperHostComponent implements OnInit, OnDestroy, OnChanges {
  @Input() documentId: string;
  @Input() inputName: string;
  @Input() inputDescription: string;
  @Input() inputDocumentType: DocumentType;
  @Input() inputInspectionType: InspectionType;
  @Input() inputDataShape: string;
  @Input() outputName: string;
  @Input() outputDescription: string;
  @Input() outputDocumentType: DocumentType;
  @Input() outputInspectionType: InspectionType;
  @Input() outputDataShape: string;
  @Input() mappings?: string;
  @Output() outputMappings = new EventEmitter<string>();
  @ViewChild('dataMapperComponent')
  private dataMapperComponent: DataMapperAppComponent;
  private saveMappingSubscription: Subscription;
  private modifiedMappings?: string;

  constructor(private initializationService: InitializationService) {}

  ngOnInit() {
    this.reset();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (
      Object.keys(changes).length === 1 &&
      (changes['mappings'] &&
        changes['mappings'].previousValue !== this.modifiedMappings)
    ) {
      // do nothing, it's us updating the mappings and getting it back from the app component
    } else {
      this.reset();
    }
  }

  reset() {
    if (this.saveMappingSubscription) {
      this.saveMappingSubscription.unsubscribe();
    }
    this.initializationService.resetConfig();

    // initialize config information before initializing services
    const c: ConfigModel = this.initializationService.cfg;

    // initialize base urls for our service calls
    c.initCfg.baseJavaInspectionServiceUrl = '/api/v1/atlas/java/';
    c.initCfg.baseXMLInspectionServiceUrl = '/api/v1/atlas/xml/';
    c.initCfg.baseJSONInspectionServiceUrl = '/api/v1/atlas/json/';
    c.initCfg.baseMappingServiceUrl = '/api/v1/atlas/';

    // // enable the navigation bar and import/export for stand-alone
    c.initCfg.disableNavbar = true;
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
    inputDoc.id = this.documentId;
    inputDoc.name = this.inputName;
    inputDoc.description = this.inputDescription;
    inputDoc.isSource = true;
    inputDoc.showFields = true;
    c.addDocument(inputDoc);

    const outputDoc: DocumentInitializationModel = new DocumentInitializationModel();
    outputDoc.type = this.outputDocumentType;
    outputDoc.inspectionType = this.outputInspectionType;
    outputDoc.inspectionSource = this.outputDataShape;
    outputDoc.id = this.documentId;
    outputDoc.name = this.outputName;
    outputDoc.description = this.outputDescription;
    outputDoc.isSource = false;
    outputDoc.showFields = true;
    c.addDocument(outputDoc);

    const mappingDefinition = new MappingDefinition();
    if (this.mappings) {
      try {
        MappingSerializer.deserializeMappingServiceJSON(
          JSON.parse(this.mappings),
          mappingDefinition,
          c
        );
      } catch (err) {
        // TODO popup or error alert?  At least catch this so we initialize
        console.error(err);
      }
    }
    c.mappings = mappingDefinition;

    this.saveMappingSubscription = c.mappingService.saveMappingOutput$.subscribe(
      (saveHandler: Function) => {
        const json = c.mappingService.serializeMappingsToJSON();
        this.modifiedMappings = JSON.stringify(json);
        this.outputMappings.emit(this.modifiedMappings);
        c.mappingService.handleMappingSaveSuccess(saveHandler);
      }
    );

    this.initializationService.initialize();
  }

  ngOnDestroy() {
    if (this.saveMappingSubscription) {
      this.saveMappingSubscription.unsubscribe();
    }
  }
}
