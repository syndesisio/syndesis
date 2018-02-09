import { Component, ViewChild, OnInit, Input } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import {
  DocumentInitializationModel,
  DocumentType,
  InspectionType,
  MappingDefinition,
  ConfigModel,
  MappingModel,
  InitializationService,
  ErrorHandlerService,
  DocumentManagementService,
  MappingManagementService,
  MappingSerializer,
  DataMapperAppComponent
} from '@atlasmap/atlasmap.data.mapper';

import { ConfigService } from '@syndesis/ui/config.service';
import { log, getCategory } from '@syndesis/ui/logging';
import { TypeFactory } from '@syndesis/ui/model';
import { DataShape, IntegrationSupportService, Step } from '@syndesis/ui/platform';
import { CurrentFlowService, FlowEvent } from '@syndesis/ui/integration/edit-page';

/*
 * Example host component:
 *
 * https://github.com/atlasmap/atlasmap-ui/blob/master/src/app/lib/syndesis-data-mapper/components/data.mapper.example.host.component.ts
 */

const category = getCategory('data-mapper');

const MAPPING_KEY = 'atlasmapping';

@Component({
  selector: 'syndesis-data-mapper-host',
  template: `
    <div *ngIf="outstandingTasks == 0" class="data-mapper-host">
      <data-mapper #dataMapperComponent></data-mapper>
    </div>
  `,
  styles: [
    `.data-mapper-host {
        /* TODO probably a better way to set this height to the viewport */
        height: calc(100vh - 140px);
      }
    `
  ],
  providers: [
    // @FIXME - This overrides the provider singletons from this point on down the component subtree,
    //          which might lead to data inconsistencies in non-immutable service members.
    //          Track down dependency injection map and remove this after moving providers to CoreModule.
    InitializationService,
    MappingManagementService,
    ErrorHandlerService,
    DocumentManagementService
  ]
})
export class DataMapperHostComponent implements OnInit {
  routeSubscription: Subscription;
  outstandingTasks = 1;

  sourceDocTypes = [];
  targetDocTypes = [];

  @Input() position: number;

  @ViewChild('dataMapperComponent')
  dataMapperComponent: DataMapperAppComponent;

  cfg: ConfigModel = new ConfigModel();

  constructor(
    public currentFlowService: CurrentFlowService,
    public route: ActivatedRoute,
    public router: Router,
    public configService: ConfigService,
    public initializationService: InitializationService,
    public support: IntegrationSupportService,
  ) {
    this.resetConfig();
  }

  initialize() {
    this.resetConfig();
    const step = this.currentFlowService.getStep(this.position);
    let mappings = undefined;
    if (step.configuredProperties && step.configuredProperties[MAPPING_KEY]) {
      mappings = <string>step.configuredProperties[MAPPING_KEY];
    }
    this.cfg.mappings = new MappingDefinition();

    for (const pair of this.currentFlowService.getPreviousStepsWithDataShape(this.position)) {
      if (this.isSupportedDataShape(pair.step.action.descriptor.outputDataShape)) {
        this.addInitializationTask();
        this.currentFlowService.fetchOutputDataShapeFor(pair.step).then(dataShape => {
          this.addSourceDocument(pair.step.id, pair.index, dataShape);
          this.removeInitializationTask();
        })
        .catch(response => this.cfg.errorService.error(
          'Failed to load source document: ' + pair.step.id, response));
      }
    }

    // Single target document for now
    let targetPair;
    for (const pair of this.currentFlowService.getSubsequentStepsWithDataShape(this.position)) {
      if (this.isSupportedDataShape(pair.step.action.descriptor.inputDataShape)) {
        targetPair = pair;
        break;
      }
    }
    if (targetPair) {
      this.addInitializationTask();
      this.currentFlowService.fetchInputDataShapeFor(targetPair.step).then(dataShape => {
        this.addTargetDocument(targetPair.step.id, targetPair.index, dataShape);
        this.removeInitializationTask();
      })
      .catch(response => this.cfg.errorService.error(
        'Failed to load target document: ' + targetPair.step.id, response));
    }

    // TODO for now set a really long timeout
    this.cfg.initCfg.classPathFetchTimeoutInMilliseconds = 3600000;
    if (mappings) {
      const mappingDefinition = new MappingDefinition();
      // Existing mappings, load from the route
      try {
        MappingSerializer.deserializeMappingServiceJSON(
          JSON.parse(mappings),
          mappingDefinition,
          this.cfg
        );
      } catch (err) {
        // TODO popup or error alert?  At least catch this so we initialize
        log.warn('Failed to deserialize mappings: ' + err, category);
      }
      this.cfg.mappings = mappingDefinition;
    }

    // enable debug / mock data flags for data mapper
    const debugConfigKeys: string[] = [
      'addMockJSONMappings',
      'discardNonMockSources',
      'addMockJavaSingleSource',
      'addMockJavaSources',
      'addMockXMLInstanceSources',
      'addMockXMLSchemaSources',
      'addMockJSONSources',
      'addMockJSONInstanceSources',
      'addMockJSONSchemaSources',
      'addMockJavaTarget',
      'addMockXMLInstanceTarget',
      'addMockXMLSchemaTarget',
      'addMockJSONTarget',
      'addMockJSONInstanceTarget',
      'addMockJSONSchemaTarget',
      'debugDocumentServiceCalls',
      'debugMappingServiceCalls',
      'debugClassPathServiceCalls',
      'debugValidationServiceCalls',
      'debugFieldActionServiceCalls',
      'debugDocumentParsing'
    ];
    for (const debugConfigKey of debugConfigKeys) {
      let debugKeyValue = false;
      try {
        debugKeyValue = this.configService.getSettings(
          'datamapper',
          debugConfigKey
        );
      } catch (err) {
        // @TODO: Remove this try/catch once ChangeDetection is restored
      }
      this.cfg.initCfg[debugConfigKey] = debugKeyValue;
    }

    //subscribe to mapping save callback from data mapper
    this.cfg.mappingService.saveMappingOutput$.subscribe(
      (saveHandler: Function) => {
        const json = this.cfg.mappingService.serializeMappingsToJSON();
        const properties = {
          atlasmapping: JSON.stringify(json)
        };
        this.currentFlowService.events.emit({
          kind: 'integration-set-properties',
          position: this.position,
          properties: properties,
          onSave: () => {
            this.cfg.mappingService.handleMappingSaveSuccess(saveHandler);
            log.debugc(() => 'Saved mapping file: ' + json, category);
          }
        });
      }
    );

    // make sure the property is set on the integration
    this.currentFlowService.events.emit({
      kind: 'integration-set-properties',
      position: this.position,
      properties: {
        atlasmapping: mappings ? mappings : ''
      },
      onSave: () => {
        this.initializeMapper();
      }
    });
    this.removeInitializationTask();
  }

  initializeMapper() {
    if ( this.outstandingTasks == 0 ) {
      this.initializationService.initialize();
    }
  }

  addInitializationTask() {
    this.outstandingTasks += 1;
  }

  removeInitializationTask() {
    this.outstandingTasks -= 1;
    this.initializeMapper();
  }

  ngOnInit() {
    this.initialize();
  }

  private isSupportedDataShape(dataShape: DataShape): boolean {
    if (!dataShape || !dataShape.kind) {
      return false;
    }
    return ['java', 'json-instance', 'json-schema', 'xml-instance', 'xml-schema']
            .indexOf(dataShape.kind) > -1;
  }

  private addSourceDocument(documentId: string, index: number, dataShape: DataShape): boolean {
    return this.addDocument(documentId, index, dataShape, true);
  }

  private addTargetDocument(documentId: string, index: number, dataShape: DataShape): boolean {
    return this.addDocument(documentId, index, dataShape, false);
  }

  private addDocument(
    documentId: string,
    index: number,
    dataShape: DataShape,
    isSource = false
  ): boolean {
    if (!dataShape || !dataShape.kind || !dataShape.specification) {
      // skip
      return false;
    }

    const initModel: DocumentInitializationModel = new DocumentInitializationModel();
    switch (dataShape.kind) {
      case 'java':
        initModel.type = DocumentType.JAVA;
        initModel.inspectionType = InspectionType.JAVA_CLASS;
        initModel.inspectionSource = dataShape.type;
        initModel.inspectionResult = dataShape.specification;
        break;
      case 'json-instance':
        initModel.type = DocumentType.JSON;
        initModel.inspectionType = InspectionType.INSTANCE;
        initModel.inspectionSource = dataShape.specification;
        break;
      case 'json-schema':
        initModel.type = DocumentType.JSON;
        initModel.inspectionType = InspectionType.SCHEMA;
        initModel.inspectionSource = dataShape.specification;
        break;
      case 'xml-instance':
        initModel.type = DocumentType.XML;
        initModel.inspectionType = InspectionType.INSTANCE;
        initModel.inspectionSource = dataShape.specification;
        break;
      case 'xml-schema':
        initModel.type = DocumentType.XML;
        initModel.inspectionType = InspectionType.SCHEMA;
        initModel.inspectionSource = dataShape.specification;
        break;
      default:
        return false; // unsupported 'kind' of document
    }

    initModel.id = documentId;
    initModel.name = 'Step ' + (index + 1) + ' - '
        + (dataShape.name ? dataShape.name : dataShape.type);
    initModel.description = dataShape.description;
    initModel.isSource = isSource;
    this.cfg.addDocument(initModel);
    return true;
  }

  private resetConfig(): void {
    this.initializationService.resetConfig();
    this.cfg = this.initializationService.cfg;

    // TODO: Move hardcoded values to data service
    const baseUrl =
      'https://syndesis-staging.b6ff.rh-idev.openshiftapps.com/v2/atlas/';
    this.cfg.initCfg.baseJavaInspectionServiceUrl = this.fetchServiceUrl(
      'baseJavaInspectionServiceUrl',
      baseUrl + 'java/',
      this.configService
    );
    this.cfg.initCfg.baseXMLInspectionServiceUrl = this.fetchServiceUrl(
      'baseXMLInspectionServiceUrl',
      baseUrl + 'xml/',
      this.configService
    );
    this.cfg.initCfg.baseJSONInspectionServiceUrl = this.fetchServiceUrl(
      'baseJSONInspectionServiceUrl',
      baseUrl + 'json/',
      this.configService
    );
    this.cfg.initCfg.baseMappingServiceUrl = this.fetchServiceUrl(
      'baseMappingServiceUrl',
      baseUrl,
      this.configService
    );
  }

  private fetchServiceUrl(
    configKey: string,
    defaultUrl: string,
    configService: ConfigService
  ): string {
    try {
      return configService.getSettings('datamapper', configKey);
    } catch (err) {
      return defaultUrl;
    }
  }
}
