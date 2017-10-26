import {
  ChangeDetectorRef,
  Component,
  ViewChild,
  OnInit,
  OnDestroy,
  Input,
} from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { DocumentDefinition } from '@atlasmap/atlasmap.data.mapper';
import { MappingDefinition } from '@atlasmap/atlasmap.data.mapper';
import { ConfigModel } from '@atlasmap/atlasmap.data.mapper';
import { MappingModel } from '@atlasmap/atlasmap.data.mapper';

import { InitializationService } from '@atlasmap/atlasmap.data.mapper';
import { ErrorHandlerService } from '@atlasmap/atlasmap.data.mapper';
import { DocumentManagementService } from '@atlasmap/atlasmap.data.mapper';
import { MappingManagementService } from '@atlasmap/atlasmap.data.mapper';
import { MappingSerializer } from '@atlasmap/atlasmap.data.mapper';

import { DataMapperAppComponent } from '@atlasmap/atlasmap.data.mapper';

import { ConfigService } from '../../../../config.service';
import { IntegrationSupportService } from '../../../../store/integration-support.service';

import { CurrentFlow, FlowEvent } from '../../current-flow.service';
import { FlowPage } from '../../flow-page';

import { Step, DataShape, TypeFactory } from '../../../../model';

import { log, getCategory } from '../../../../logging';

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
    <div *ngIf="initialized" class="data-mapper-host">
      <data-mapper #dataMapperComponent></data-mapper>
    </div>
  `,
  styles: [
    `.data-mapper-host {
        /* TODO probably a better way to set this height to the viewport */
        height: calc(100vh - 140px);
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
export class DataMapperHostComponent extends FlowPage
  implements OnInit, OnDestroy {
  routeSubscription: Subscription;
  initialized = false;
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
    public detector: ChangeDetectorRef,
  ) {
    super(currentFlow, route, router, detector);
    this.resetConfig();
  }

  private resetConfig(): void {
    this.initializationService.resetConfig();
    this.cfg = this.initializationService.cfg;

    const baseUrl =
      'https://syndesis-staging.b6ff.rh-idev.openshiftapps.com/v2/atlas/';
    this.cfg.initCfg.baseJavaInspectionServiceUrl = this.fetchServiceUrl(
      'baseJavaInspectionServiceUrl',
      baseUrl + 'java/',
      this.configService,
    );
    this.cfg.initCfg.baseXMLInspectionServiceUrl = this.fetchServiceUrl(
      'baseXMLInspectionServiceUrl',
      baseUrl + 'xml/',
      this.configService,
    );
    this.cfg.initCfg.baseJSONInspectionServiceUrl = this.fetchServiceUrl(
      'baseJSONInspectionServiceUrl',
      baseUrl + 'json/',
      this.configService,
    );
    this.cfg.initCfg.baseMappingServiceUrl = this.fetchServiceUrl(
      'baseMappingServiceUrl',
      baseUrl,
      this.configService,
    );
  }

  private fetchServiceUrl(
    configKey: string,
    defaultUrl: string,
    configService: ConfigService,
  ): string {
    try {
      return configService.getSettings('datamapper', configKey);
    } catch (err) {
      return defaultUrl;
    }
  }

  createDocumentDefinition(connectorId: string, dataShape: DataShape, isSource: boolean = false) {
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
        const docDef: DocumentDefinition = this.cfg.addJavaDocument(type, isSource);
        this.support.requestJavaInspection(connectorId, type).subscribe(
          data => {
            const inspection: string = data['_body'];
            log.infoc(() => 'Precomputed java document found for ' + type, category);
            log.debugc(() => inspection, category);
            docDef.initCfg.inspectionResultContents = inspection;
          },
          err => {
            log.warnc(() => 'No precomputed java document found for ' + type + ': ' + err, category);
          },
        );
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

    const previousConnectorId: string = this.currentFlow.getPreviousConnection(this.position).connection.connectorId;
    const subsequentConnectorId: string = this.currentFlow.getSubsequentConnection(this.position).connection.connectorId;
    this.createDocumentDefinition(previousConnectorId, this.outputDataShape, true);
    this.createDocumentDefinition(subsequentConnectorId, this.inputDataShape, false);

    // TODO for now set a really long timeout
    this.cfg.initCfg.classPathFetchTimeoutInMilliseconds = 3600000;
    if (mappings) {
      const mappingDefinition = new MappingDefinition();
      // Existing mappings, load from the route
      try {
        MappingSerializer.deserializeMappingServiceJSON(
          JSON.parse(mappings),
          mappingDefinition,
          this.cfg,
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
      'debugDocumentParsing',
    ];
    for (const debugConfigKey of debugConfigKeys) {
      let debugKeyValue = false;
      try {
        debugKeyValue = this.configService.getSettings(
          'datamapper',
          debugConfigKey,
        );
      } catch (err) {}
      this.cfg.initCfg[debugConfigKey] = debugKeyValue;
    }

    //subscribe to mapping save callback from data mapper
    this.cfg.mappingService.saveMappingOutput$.subscribe(
      (saveHandler: Function) => {
        const json = this.cfg.mappingService.serializeMappingsToJSON();
        const properties = {
          atlasmapping: JSON.stringify(json),
        };
        this.currentFlow.events.emit({
          kind: 'integration-set-properties',
          position: this.position,
          properties: properties,
          onSave: () => {
            this.cfg.mappingService.handleMappingSaveSuccess(saveHandler);
            log.debugc(() => 'Saved mapping file: ' + json, category);
          },
        });
      },
    );

    // make sure the property is set on the integration
    this.currentFlow.events.emit({
      kind: 'integration-set-properties',
      position: this.position,
      properties: {
        atlasmapping: mappings ? mappings : '',
      },
      onSave: () => {
        setTimeout(() => {
          this.initializeMapper();
        }, 10);
      },
    });
  }

  initializeMapper() {
    this.cfg.mappingService.mappingUpdated$.subscribe(() => {
      this.detector.detectChanges();
    });
    this.cfg.initializationService.systemInitialized$.subscribe(() => {
      this.detector.detectChanges();
    });
    this.initialized = true;
    this.initializationService.initialize();
    this.detector.detectChanges();
    /*
    log.debugc(() => 'Fetching POM for integration', category);
    this.support.requestPom(this.currentFlow.getIntegrationClone()).subscribe(
      data => {
        const pom = data['_body'];
        log.debugc(() => 'Fetched POM for integration: ' + pom, category);
        this.cfg.initCfg.classPath = null;
        this.cfg.initCfg.pomPayload = pom;
        this.initializationService.initialize();
        this.detector.detectChanges();
      },
      err => {
        // do our best I guess
        try {
          log.warnc(
            () => 'failed to fetch pom: ' + JSON.parse(err['_body']),
            category,
          );
        } catch (err) {
          log.warnc(() => 'failed to fetch pom: ' + err['_body'], category);
        }
        this.cfg.initCfg.classPath = '';
        this.initializationService.initialize();
        this.detector.detectChanges();
      },
    );
    */
  }

  ngOnInit() {
    this.initialize();
  }

  ngOnDestroy() {}
}
