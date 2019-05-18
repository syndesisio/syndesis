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
  DocumentManagementService,
  ErrorHandlerService,
  InitializationService,
  MappingManagementService,
  MappingDefinition,
  MappingSerializer,
} from '@atlasmap/atlasmap-data-mapper';
import { Subscription } from 'rxjs';
import { environment } from '../environments/environment';
import { IDocumentProps } from './app.component';

@Component({
  selector: 'app-apicurio-host',
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
  @Input() inputDocuments: IDocumentProps[];
  @Input() outputDocument: IDocumentProps;
  @Input() initialMappings?: string;
  @Input() baseJavaInspectionServiceUrl: string;
  @Input() baseXMLInspectionServiceUrl: string;
  @Input() baseJSONInspectionServiceUrl: string;
  @Input() baseMappingServiceUrl: string;
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
      (changes['initialMappings'] &&
        changes['initialMappings'].previousValue !== this.modifiedMappings)
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

    c.initCfg.xsrfCookieName = environment.xsrf.cookieName;
    c.initCfg.xsrfDefaultTokenValue = environment.xsrf.defaultTokenValue;
    c.initCfg.xsrfHeaderName = environment.xsrf.headerName;

    const makeUrl = (url: string) => {
      return !url.startsWith('http') &&
        !url.startsWith(this.baseMappingServiceUrl)
        ? this.baseMappingServiceUrl + url
        : url;
    };

    // initialize base urls for our service calls
    c.initCfg.baseMappingServiceUrl = this.baseMappingServiceUrl;
    c.initCfg.baseJavaInspectionServiceUrl = makeUrl(
      this.baseJavaInspectionServiceUrl
    );
    c.initCfg.baseXMLInspectionServiceUrl = makeUrl(
      this.baseXMLInspectionServiceUrl
    );
    c.initCfg.baseJSONInspectionServiceUrl = makeUrl(
      this.baseJSONInspectionServiceUrl
    );

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

    this.inputDocuments.forEach(d => {
      const inputDoc: DocumentInitializationModel = new DocumentInitializationModel();
      inputDoc.type = d.documentType;
      inputDoc.inspectionType = d.inspectionType;
      inputDoc.inspectionSource = d.inspectionSource;
      inputDoc.inspectionResult = d.inspectionResult;
      inputDoc.id = d.id;
      inputDoc.name = d.name;
      inputDoc.description = d.description;
      inputDoc.isSource = true;
      inputDoc.showFields = d.showFields;
      c.addDocument(inputDoc);
    });

    const outputDoc: DocumentInitializationModel = new DocumentInitializationModel();
    outputDoc.type = this.outputDocument.documentType;
    outputDoc.inspectionType = this.outputDocument.inspectionType;
    outputDoc.inspectionSource = this.outputDocument.inspectionSource;
    outputDoc.id = this.outputDocument.id;
    outputDoc.name = this.outputDocument.name;
    outputDoc.description = this.outputDocument.description;
    outputDoc.isSource = false;
    outputDoc.showFields = this.outputDocument.showFields;
    c.addDocument(outputDoc);

    const mappingDefinition = new MappingDefinition();
    if (this.initialMappings) {
      try {
        MappingSerializer.deserializeMappingServiceJSON(
          JSON.parse(this.initialMappings),
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
