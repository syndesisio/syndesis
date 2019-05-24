import {
  Component,
  ViewChild,
  OnInit,
  OnDestroy,
  Input,
  Output,
  EventEmitter,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';

import {
  DocumentInitializationModel,
  DocumentType,
  InspectionType,
  MappingDefinition,
  ConfigModel,
  InitializationService,
  ErrorHandlerService,
  DocumentManagementService,
  MappingManagementService,
  MappingSerializer,
  DataMapperAppComponent,
} from '@atlasmap/atlasmap-data-mapper';

import { ConfigService } from '@syndesis/ui/config.service';
import {
  DataShapeKinds,
  DataShape,
  IntegrationSupportService,
  Step,
  ActionDescriptor,
  Action,
} from '@syndesis/ui/platform';
import {
  CurrentFlowService,
  FlowPageService,
  INTEGRATION_SET_ACTION,
} from '@syndesis/ui/integration/edit-page';
import {
  INTEGRATION_SIDEBAR_COLLAPSE,
  IndexedStep,
} from '../../edit-page.models';
import { SPLIT, AGGREGATE } from '@syndesis/ui/store';
import { debounceTime, tap } from 'rxjs/operators';
/*
 * Example host component:
 *
 * https://github.com/atlasmap/atlasmap-ui/blob/master/src/app/lib/syndesis-data-mapper/components/data.mapper.example.host.component.ts
 */

const MAPPING_KEY = 'atlasmapping';

@Component({
  selector: 'syndesis-data-mapper-host',
  template: `
    <div
      *ngIf="outstandingTasks == 0"
      [ngClass]="
        currentFlowService.isApiProvider()
          ? 'api-provider-data-mapper-host'
          : 'data-mapper-host'
      "
    >
      <data-mapper #dataMapperComponent></data-mapper>
    </div>
  `,
  styles: [
    `
      /* The host height for an API provider flow */
      .api-provider-data-mapper-host {
        height: calc(100vh - 265px);
      }

      /* The host height for a regular flow */
      .data-mapper-host {
        height: calc(100vh - 195px);
      }
    `,
  ],
  providers: [
    InitializationService,
    MappingManagementService,
    ErrorHandlerService,
    DocumentManagementService,
  ],
})
export class DataMapperHostComponent implements OnInit, OnDestroy {
  outstandingTasks = 1;

  sourceDocTypes = [];
  targetDocTypes = [];

  @Input() position: number;
  @Output() mappings = new EventEmitter<string>();

  @Input() valid: boolean;
  @Output() validChange = new EventEmitter<boolean>();

  @ViewChild('dataMapperComponent') dataMapperComponent: DataMapperAppComponent;

  cfg: ConfigModel = new ConfigModel();

  private saveMappingHandlerSubscription: Subscription;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    public configService: ConfigService,
    public initializationService: InitializationService,
    public support: IntegrationSupportService
  ) {}

  initialize() {
    this.resetConfig();
    const step = this.currentFlowService.getStep(this.position);

    let foundDocuments = false;
    if (this.populateSourceDocuments()) {
      foundDocuments = this.populateTargetDocument(step);
    }

    let mappings = '';
    let onSave = undefined;
    if (foundDocuments) {
      mappings = this.initializeMappingDefinition(step);
      this.setupDebugKeys();
      this.saveMappingHandlerSubscription = this.registerSaveMappingHandler();
      onSave = () => {
        this.initializeMapper();
      };
    } else {
      this.cfg.sourceDocs = [];
      this.cfg.targetDocs = [];
    }
    this.mappings.emit(mappings);
    if (onSave) {
      onSave();
    }
    this.removeInitializationTask();
  }

  initializeMapper() {
    if (this.outstandingTasks == 0) {
      this.initializationService.initialize();
      // TODO: defers this until the next change detection, should
      // be a candidate for removal though.
      setTimeout(() => {
        // Set this to true always for now, defer emitting it until
        // the next change detection cycle
        this.validChange.emit(true);
        // manually triggers the mapper's line machine
        this.cfg.mappingService.mappingUpdatedSource.next();
      }, 1);
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
    this.flowPageService.showDone = true;
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SIDEBAR_COLLAPSE,
    });
    this.initialize();
  }

  ngOnDestroy() {
    this.flowPageService.showDone = false;
    if (this.saveMappingHandlerSubscription) {
      this.saveMappingHandlerSubscription.unsubscribe();
    }
  }

  private setupDebugKeys() {
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
      'debugDocumentParsing',
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
  }

  // Narrow down the previous steps to a subset based on any "scope" introduced by other steps
  private restrictPreviousStepArrayScope(
    previousSteps: IndexedStep[],
    stepKind
  ): IndexedStep[] {
    const splitIndex = previousSteps
      .reverse()
      .findIndex(s => s.step.stepKind === stepKind);
    if (splitIndex === -1) {
      return previousSteps.reverse();
    } else {
      return previousSteps.slice(0, splitIndex + 1).reverse();
    }
  }

  private populateSourceDocuments(): boolean {
    // Fetch the previous steps with data shapes, but observe any "scope" created by a previous split step
    const previousSteps = this.restrictPreviousStepArrayScope(
      this.restrictPreviousStepArrayScope(
        this.currentFlowService.getPreviousStepsWithDataShape(this.position),
        SPLIT
      ),
      AGGREGATE
    );
    if (!previousSteps || previousSteps.length === 0) {
      this.cfg.errorService.error(
        'No source data type was found. Data Mapper requires at least one data type aware step prior to itself.',
        ''
      );
      return false;
    }

    // Populate all supported DataShape from previous DataShape aware steps as source documents
    let hasSource = false;
    const dataShapeAwareSteps = previousSteps.filter(pair =>
      this.isSupportedDataShape(pair.step.action.descriptor.outputDataShape)
    );
    for (const pair of dataShapeAwareSteps) {
      const outputDataShape = pair.step.action.descriptor.outputDataShape;
      if (
        this.addSourceDocument(
          pair.step.id,
          pair.index,
          outputDataShape,
          dataShapeAwareSteps.length === 1
        )
      ) {
        hasSource = true;
      }
    }
    if (!hasSource) {
      this.cfg.errorService.error(
        'No supported source data type was found. Data type needs to be configured before Data Mapper step is added.',
        ''
      );
      return false;
    }
    return true;
  }

  private populateTargetDocument(step: Step): boolean {
    // Next DataShape aware step must have a supported input DataShape, which describes a target document
    const subsequents = this.currentFlowService.getSubsequentStepsWithDataShape(
      this.position
    );
    // The first step could be this datamapper step itself if it's not the first visit,
    // as DataShape is added by the following event
    let targetPair = subsequents[0];
    if (targetPair.step.id === step.id) {
      targetPair = subsequents[1];
    }
    if (!targetPair) {
      this.cfg.errorService.error(
        'No target data type was found. Data Mapper step can only be added before data type aware step.',
        ''
      );
      return false;
    }
    const inputDataShape = targetPair.step.action.descriptor.inputDataShape;
    if (!inputDataShape.kind || !inputDataShape.specification) {
      this.cfg.errorService.error(
        'No data type specification was found for subsequent step',
        ''
      );
      return false;
    }
    if (
      !this.addTargetDocument(
        targetPair.step.id,
        targetPair.index,
        inputDataShape,
        true
      )
    ) {
      this.cfg.errorService.error(
        'Unsupported data type was found for subsequent step',
        ''
      );
      return false;
    }
    this.addInitializationTask();
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SET_ACTION,
      position: this.position,
      stepKind: 'mapper',
      action: {
        actionType: 'step',
        descriptor: {
          inputDataShape: {
            kind: DataShapeKinds.ANY,
            name: 'All preceding outputs',
          },
          outputDataShape: {
            kind: inputDataShape.kind,
            type: inputDataShape.type,
            name: 'Data Mapper (' + inputDataShape.name + ')',
            description: inputDataShape.description,
            specification: inputDataShape.specification,
          },
        } as ActionDescriptor,
      } as Action,
      onSave: () => {
        this.removeInitializationTask();
      },
    });
    return true;
  }

  private initializeMappingDefinition(step: Step): string {
    this.cfg.mappings = new MappingDefinition();
    let mappings = undefined;
    if (step.configuredProperties && step.configuredProperties[MAPPING_KEY]) {
      mappings = <string>step.configuredProperties[MAPPING_KEY];
    }

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
        this.cfg.errorService.error(
          'Failed to deserialize mappings: ' + err,
          ''
        );
      }
      this.cfg.mappings = mappingDefinition;
    }
    return mappings;
  }

  private registerSaveMappingHandler(): Subscription {
    //subscribe to mapping save callback from data mapper
    return this.cfg.mappingService.saveMappingOutput$
      .pipe(
        tap(saveHandler =>
          this.cfg.mappingService.handleMappingSaveSuccess(saveHandler)
        ),
        debounceTime(15)
      )
      .subscribe(async (saveHandler: Function) => {
        const json = this.cfg.mappingService.serializeMappingsToJSON();
        this.mappings.emit(JSON.stringify(json));
      });
  }

  private isSupportedDataShape(dataShape: DataShape): boolean {
    if (!dataShape || !dataShape.kind) {
      return false;
    }
    return (
      [
        DataShapeKinds.JAVA,
        DataShapeKinds.JSON_INSTANCE,
        DataShapeKinds.JSON_SCHEMA,
        DataShapeKinds.XML_INSTANCE,
        DataShapeKinds.XML_SCHEMA,
        DataShapeKinds.XML_SCHEMA_INSPECTED,
      ].indexOf(dataShape.kind) > -1
    );
  }

  private addSourceDocument(
    documentId: string,
    index: number,
    dataShape: DataShape,
    showFields: boolean
  ): boolean {
    return this.addDocument(documentId, index, dataShape, true, showFields);
  }

  private addTargetDocument(
    documentId: string,
    index: number,
    dataShape: DataShape,
    showFields: boolean
  ): boolean {
    return this.addDocument(documentId, index, dataShape, false, showFields);
  }

  private addDocument(
    documentId: string,
    index: number,
    dataShape: DataShape,
    isSource = false,
    showFields = true
  ): boolean {
    if (!dataShape || !dataShape.kind || !dataShape.specification) {
      // skip
      return false;
    }

    const initModel: DocumentInitializationModel = new DocumentInitializationModel();
    switch (dataShape.kind) {
      case DataShapeKinds.JAVA:
        initModel.type = DocumentType.JAVA;
        initModel.inspectionType = InspectionType.JAVA_CLASS;
        initModel.inspectionSource = dataShape.type;
        initModel.inspectionResult = dataShape.specification;
        break;
      case DataShapeKinds.JSON_INSTANCE:
        initModel.type = DocumentType.JSON;
        initModel.inspectionType = InspectionType.INSTANCE;
        initModel.inspectionSource = dataShape.specification;
        break;
      case DataShapeKinds.JSON_SCHEMA:
        initModel.type = DocumentType.JSON;
        initModel.inspectionType = InspectionType.SCHEMA;
        initModel.inspectionSource = dataShape.specification;
        break;
      case DataShapeKinds.XML_INSTANCE:
        initModel.type = DocumentType.XML;
        initModel.inspectionType = InspectionType.INSTANCE;
        initModel.inspectionSource = dataShape.specification;
        break;
      case DataShapeKinds.XML_SCHEMA:
        initModel.type = DocumentType.XML;
        initModel.inspectionType = InspectionType.SCHEMA;
        initModel.inspectionSource = dataShape.specification;
        break;
      case DataShapeKinds.XML_SCHEMA_INSPECTED:
        initModel.type = DocumentType.XML;
        initModel.inspectionType = InspectionType.SCHEMA;
        initModel.inspectionResult = dataShape.specification;
        break;
      default:
        return false; // unsupported 'kind' of document
    }

    initModel.id = documentId;
    initModel.name =
      index + 1 + ' - ' + (dataShape.name ? dataShape.name : dataShape.type);
    initModel.description = dataShape.description;
    initModel.isSource = isSource;
    initModel.showFields = showFields;

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

    try {
      this.cfg.initCfg.disableMappingPreviewMode = this.configService.getSettings(
        'datamapper',
        'disableMappingPreviewMode'
      );
    } catch (err) {
      this.cfg.initCfg.disableMappingPreviewMode = true;
    }
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
