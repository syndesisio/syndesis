import {
  Component,
  ViewChild,
  OnInit,
  Input
} from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import {
  DocumentDefinition,
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
import { Step } from '@syndesis/ui/integration';
import { IntegrationSupportService } from '../../../integration-support.service';
import { CurrentFlow, FlowEvent, FlowPage } from '@syndesis/ui/integration/edit-page';
import { DataShape, TypeFactory } from '@syndesis/ui/model';
import { log, getCategory } from '@syndesis/ui/logging';

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
export class DataMapperHostComponent extends FlowPage implements OnInit {
  routeSubscription: Subscription;
  outstandingTasks = 1;

  sourceDocTypes = [];
  targetDocTypes = [];

  @Input() position: number;
  @Input() step: Step;
  @Input() inputDataShape: DataShape;
  @Input() outputDataShape: DataShape;

  @ViewChild('dataMapperComponent')
  public dataMapperComponent: DataMapperAppComponent;

  public cfg: ConfigModel = new ConfigModel();

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public configService: ConfigService,
    public initializationService: InitializationService,
    public support: IntegrationSupportService,
  ) {
    super(currentFlow, route, router);
    this.resetConfig();
  }

  createDocumentDefinition(
    connectorId: string,
    dataShape: DataShape,
    isSource = false
  ) {
    if (!dataShape || !dataShape.kind) {
      // skip
      return;
    }
    const type = dataShape.type;
    const kind = dataShape.kind;
    const specification = dataShape.specification || '';
    // TODO not sure what to do for `none` or `any` here
    switch (kind) {
      case 'java':
        const docDef = this.cfg.addJavaDocument(type, isSource);
        if (specification != '') {
          docDef.initCfg.inspectionResultContents = specification;
        } else {
          this.addInitializationTask();
          this.support.requestJavaInspection(connectorId, type).toPromise().then(
            data => {
              const inspection: string = data['_body'] || data;
              log.infoc( () => `Precomputed java document found for ${type}`, category);
              log.debugc(() => inspection, category);
              docDef.initCfg.inspectionResultContents = inspection;
              this.removeInitializationTask();
            },
            err => {
              log.warnc(() => `No precomputed java document found for ${type}: ${err}`, category);
              this.removeInitializationTask();
            }
          );
        }
        break;
      case 'json':
      case 'json-instance':
        this.cfg.addJSONInstanceDocument(type, specification, isSource);
        break;
      case 'json-schema':
        this.cfg.addJSONSchemaDocument(type, specification, isSource);
        break;
      case 'xml-instance':
        this.cfg.addXMLInstanceDocument(type, specification, isSource);
        break;
      case 'xml-schema':
        this.cfg.addXMLSchemaDocument(type, specification, isSource);
        break;
      default:
        break;
    }
  }

  initialize() {
    this.resetConfig();
    const step = this.step;
    let mappings = undefined;
    if (step.configuredProperties && step.configuredProperties[MAPPING_KEY]) {
      mappings = <string>step.configuredProperties[MAPPING_KEY];
    }
    this.cfg.mappings = new MappingDefinition();

    const previousConnectorId: string = this.currentFlow.getPreviousConnection(
      this.position
    ).connection.connectorId;
    const subsequentConnectorId: string = this.currentFlow.getSubsequentConnection(
      this.position
    ).connection.connectorId;
    this.createDocumentDefinition(
      previousConnectorId,
      this.outputDataShape,
      true
    );
    this.createDocumentDefinition(
      subsequentConnectorId,
      this.inputDataShape,
      false
    );

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
        this.currentFlow.events.emit({
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
    this.currentFlow.events.emit({
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
