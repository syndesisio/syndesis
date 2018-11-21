import { Component, ViewChild, OnInit, OnDestroy } from '@angular/core';
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
  template: '<data-mapper #dataMapperComponent></data-mapper>',
  providers: [
    MappingManagementService,
    ErrorHandlerService,
    DocumentManagementService,
  ],
})
export class DataMapperHostComponent implements OnInit, OnDestroy {
  @ViewChild('dataMapperComponent')
  dataMapperComponent: DataMapperAppComponent;

  private saveMappingSubscription: Subscription;

  constructor(private initializationService: InitializationService) {}

  ngOnInit() {
    // initialize config information before initializing services
    const c: ConfigModel = this.initializationService.cfg;

    // store references to our services in our config model

    // initialize base urls for our service calls
    c.initCfg.baseJavaInspectionServiceUrl = '/api/v1/atlas/java/';
    c.initCfg.baseXMLInspectionServiceUrl = '/api/v1/atlas/xml/';
    c.initCfg.baseJSONInspectionServiceUrl = '/api/v1/atlas/json/';
    c.initCfg.baseMappingServiceUrl = '/api/v1/atlas/';
    //
    // // initialize data for our class path service call
    // // note that quotes, newlines, and tabs are escaped
    // // c.initCfg.pomPayload = InitializationService.createExamplePom();
    // // c.initCfg.classPathFetchTimeoutInMilliseconds = 30000;
    // // // if classPath is specified, maven call to resolve pom will be skipped
    // // c.initCfg.classPath = null;
    //
    //
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
    //
    // /*
    //  * The following examples demonstrate adding source/target documents to the Data Mapper's configuration.
    //  * Note that multiple source documents are supported, but multiple target documents are not supported.
    //  *
    //  * example java source document configuration:
    //  *
    //  * var documentIsSourceDocument: boolean = true;
    //  * c.addJavaDocument("io.atlasmap.java.test.SourceOrder", documentIsSourceDocument);
    //  *
    //  * example xml instance document:
    //  *
    //  * c.addXMLInstanceDocument("XMLInstanceSource", DocumentManagementService.generateMockInstanceXML(), documentIsSourceDocument);
    //  *
    //  * example xml schema document:
    //  *
    //  * c.addXMLSchemaDocument("XMLSchemaSource", DocumentManagementService.generateMockSchemaXML(), documentIsSourceDocument);
    //  *
    //  * example json document:
    //  *
    //  * c.addJSONDocument("JSONTarget", DocumentManagementService.generateMockJSON(), documentIsSourceDocument);
    //  *
    //  */
    //
    // enable debug logging options as needed
    c.initCfg.debugDocumentServiceCalls = true;
    c.initCfg.debugDocumentParsing = false;
    c.initCfg.debugMappingServiceCalls = false;
    c.initCfg.debugClassPathServiceCalls = false;
    c.initCfg.debugValidationServiceCalls = false;
    c.initCfg.debugFieldActionServiceCalls = false;

    // // save the mappings when the ui calls us back asking for save
    // this.saveMappingSubscription
    //   = c.mappingService.saveMappingOutput$.subscribe((saveHandler: Function) => {
    //   // NOTE: the mapping definition being saved is currently stored in "this.cfg.mappings" until further notice.
    //
    //   // This is an example callout to save the mapping to the mock java service
    //   c.mappingService.saveMappingToService();
    //
    //   // After you've sucessfully saved you *MUST* call this (don't call on error)
    //   c.mappingService.handleMappingSaveSuccess(saveHandler);
    // });
    //
    const left: DocumentInitializationModel = new DocumentInitializationModel();
    left.type = DocumentType.JSON;
    left.inspectionType = InspectionType.SCHEMA;
    left.inspectionSource = JSON.stringify({
      type: 'object',
      $schema: 'http://json-schema.org/schema#',
      title: 'create_lead_OUT',
      properties: {
        first_name: {
          type: 'string',
          required: true,
        },
        last_name: {
          type: 'string',
          required: true,
        },
        company: {
          type: 'string',
          required: true,
        },
        lead_source: {
          type: 'string',
          required: true,
        },
      },
    });
    left.id = 'sql-stored-start-connector';
    left.name = 'sql-stored-start-connector';
    left.description = 'Return value of Stored Procedure "create_lead"';
    left.isSource = true;
    left.showFields = true;
    c.addDocument(left);

    const right: DocumentInitializationModel = new DocumentInitializationModel();
    right.type = DocumentType.JSON;
    right.inspectionType = InspectionType.SCHEMA;
    right.inspectionSource = JSON.stringify({
      type: 'object',
      $schema: 'http://json-schema.org/schema#',
      title: 'add_lead_IN',
      properties: {
        first_and_last_name: {
          type: 'string',
          required: true,
        },
        company: {
          type: 'string',
          required: true,
        },
        phone: {
          type: 'string',
          required: true,
        },
        email: {
          type: 'string',
          required: true,
        },
        lead_source: {
          type: 'string',
          required: true,
        },
        lead_status: {
          type: 'string',
          required: true,
        },
        rating: {
          type: 'string',
          required: true,
        },
      },
    });
    right.id = 'add_lead Parameter';
    right.name = 'add_lead Parameter';
    right.description = 'Parameters of Stored Procedure "add_lead"';
    right.isSource = false;
    right.showFields = true;
    c.addDocument(right);

    c.mappings = new MappingDefinition();

    this.initializationService.initialize();
  }

  ngOnDestroy() {
    this.saveMappingSubscription.unsubscribe();
  }
}
