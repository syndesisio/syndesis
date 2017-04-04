import { ChangeDetectorRef, Component, ViewChild, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { DocumentDefinition } from 'ipaas.data.mapper';
import { MappingDefinition } from 'ipaas.data.mapper';
import { ConfigModel } from 'ipaas.data.mapper';
import { MappingModel } from 'ipaas.data.mapper';

import { InitializationService } from 'ipaas.data.mapper';
import { ErrorHandlerService } from 'ipaas.data.mapper';
import { DocumentManagementService } from 'ipaas.data.mapper';
import { MappingManagementService } from 'ipaas.data.mapper';

import { DataMapperAppComponent } from 'ipaas.data.mapper';

import { ConfigService } from '../../../config.service';
import { IntegrationSupportService } from '../../../store/integration-support.service';

import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { FlowPage } from '../flow-page';

import { Step, DataShape, TypeFactory } from '../../../model';

import { log, getCategory } from '../../../logging';

const category = getCategory('Connections');

const MAPPING_KEY = 'atlasmapping';

@Component({
  selector: 'ipaas-data-mapper-host',
  template: `
    <div *ngIf="initialized">
      <data-mapper #dataMapperComponent [cfg]="cfg"></data-mapper>
    </div>
  `,
  providers: [ConfigService, MappingManagementService, ErrorHandlerService, DocumentManagementService],
})
export class DataMapperHostComponent extends FlowPage implements OnInit, OnDestroy {

  routeSubscription: Subscription;
  position: number;
  initialized = false;

  @ViewChild('dataMapperComponent')
  public dataMapperComponent: DataMapperAppComponent;

  public cfg: ConfigModel = new ConfigModel();

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public documentService: DocumentManagementService,
    public mappingService: MappingManagementService,
    public errorService: ErrorHandlerService,
    public configService: ConfigService,
    public initializationService: InitializationService,
    public support: IntegrationSupportService,
    public detector: ChangeDetectorRef,
  ) {
    super(currentFlow, route, router);
    documentService.cfg = this.cfg;
    mappingService.cfg = this.cfg;
    initializationService.cfg = this.cfg;
    this.cfg.initializationService = initializationService;
    this.cfg.documentService = documentService;
    this.cfg.mappingService = mappingService;
    this.cfg.errorService = errorService;
    try {
      this.cfg.baseJavaServiceUrl = configService.getSettings('datamapper', 'baseJavaServiceUrl') || this.cfg.baseJavaServiceUrl;
      this.cfg.baseMappingServiceUrl = configService.getSettings('datamapper', 'baseMappingServiceUrl') || this.cfg.baseMappingServiceUrl;
    } catch (err) {
      // run with defaults
      this.cfg.baseJavaServiceUrl = 'https://ipaas-staging.b6ff.rh-idev.openshiftapps.com/v2/atlas/java/';
      this.cfg.baseMappingServiceUrl = 'https://ipaas-staging.b6ff.rh-idev.openshiftapps.com/v2/atlas/';
    }
  }

  createDocumentDefinition(dataShape: DataShape, isSource: boolean = false) {
    const answer = new DocumentDefinition();
    answer.isSource = isSource;
    answer.initCfg.documentIdentifier = dataShape['dataType'].replace(/java:/, '');
    return answer;
  }

  initialize() {
    const step = this.currentFlow.getStep(this.position);
    let mappings = undefined;
    if (step.configuredProperties && step.configuredProperties[MAPPING_KEY]) {
      try {
        mappings = <any>step.configuredProperties[MAPPING_KEY]['AtlasMapping'];
      } catch (err) {
        // TODO
      }
    }
    this.cfg.mappings = new MappingDefinition();
    this.cfg.classPath = '';

    const start = this.currentFlow.getStep(this.currentFlow.getFirstPosition());
    const end = this.currentFlow.getStep(this.currentFlow.getLastPosition());
    // TODO we'll want to parse the dataType and maybe set the right config value
    const inputDocDef = this.createDocumentDefinition(start.action.outputDataShape, true);
    this.cfg.sourceDocs.push(inputDocDef);

    const outputDocDef = this.createDocumentDefinition(end.action.inputDataShape);
    this.cfg.targetDocs.push(outputDocDef);

    if (mappings) {
      const mappingDefinition = new MappingDefinition();
      // Existing mappings, load from the route
      this.mappingService.deserializeMappingServiceJSON(mappings, mappingDefinition);
      this.cfg.mappings = mappingDefinition;
    }
    this.mappingService.saveMappingOutput$.subscribe((saveHandler: Function) => {
      const json = this.mappingService.serializeMappingsToJSON(this.cfg.mappings);
      const properties = {
        atlasMapping: json,
      };
      this.currentFlow.events.emit({
        kind: 'integration-set-properties',
        position: this.position,
        properties: properties,
        onSave: () => {
          log.debugc(() => 'Saved mapping file: ' + json, category);
        },
      });
    });
    // make sure the property is set on the integration
    this.currentFlow.events.emit({
      kind: 'integration-set-properties',
      position: this.position,
      properties: {
        atlasMapping: mappings ? JSON.stringify(mappings) : '',
      },
      onSave: () => {
        this.initializeMapper();
      },
    });
  }

  initializeMapper() {
    log.debugc(() => 'Fetching POM for integration', category);
    this.support.requestPom(this.currentFlow.getIntegrationClone()).subscribe(
      (data) => {
        const pom = data['_body'];
        log.debugc(() => 'Fetched POM for integration: ' + pom, category);
        this.cfg.pomPayload = pom;
        this.initializationService.initialize();
        this.initialized = true;
        this.detector.detectChanges();
      }, (err) => {
        // do our best I guess
        log.warnc(() => 'failed to fetch pom: ', JSON.parse(err), category);
        this.initializationService.initialize();
        this.initialized = true;
        this.detector.detectChanges();
      });
  }

  ngOnInit() {
    this.routeSubscription = this.route.params.pluck<Params, string>('position')
      .map((position: string) => {
        this.position = Number.parseInt(position);
        setTimeout(() => {
          this.initialize();
        }, 10);
      })
      .subscribe();
  }

  ngOnDestroy() {
    this.routeSubscription.unsubscribe();
  }


}
