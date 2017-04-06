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
    <div *ngIf="initialized" class="data-mapper-host">
      <data-mapper #dataMapperComponent [cfg]="cfg"></data-mapper>
    </div>
  `,
  styles: [
    `.data-mapper-host {
        /* TODO probably a better way to set this height to the viewport */
        height: calc(100vh - 140px);
      }
    `,
  ],
  providers: [InitializationService, ConfigService, MappingManagementService, ErrorHandlerService, DocumentManagementService],
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
      this.cfg.initCfg.baseJavaServiceUrl = configService.getSettings('datamapper', 'baseJavaServiceUrl');
      this.cfg.initCfg.baseMappingServiceUrl = configService.getSettings('datamapper', 'baseMappingServiceUrl');
    } catch (err) {
      // run with defaults
      this.cfg.initCfg.baseJavaServiceUrl = 'https://ipaas-staging.b6ff.rh-idev.openshiftapps.com/v2/atlas/java/';
      this.cfg.initCfg.baseMappingServiceUrl = 'https://ipaas-staging.b6ff.rh-idev.openshiftapps.com/v2/atlas/';
    }
  }

  createDocumentDefinition(dataShape: DataShape, isSource: boolean = false) {
    const answer = new DocumentDefinition();
    answer.isSource = isSource;
    // TODO not sure what to do for `none` or `any` here
    switch (dataShape.kind) {
      case 'java':
        answer.initCfg.documentIdentifier = dataShape.type;
        break;
    }
    return answer;
  }

  initialize() {
    const step = this.currentFlow.getStep(this.position);
    let mappings = undefined;
    if (step.configuredProperties && step.configuredProperties[MAPPING_KEY]) {
      mappings = <string>step.configuredProperties[MAPPING_KEY];
    }
    this.cfg.mappings = new MappingDefinition();

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
      try {
        this.mappingService.deserializeMappingServiceJSON(JSON.parse(mappings), mappingDefinition);
      } catch (err) {
        // TODO popup or error alert?  At least catch this so we initialize
        log.warn('Failed to deserialize mappings: ' + err, category);
      }
      this.cfg.mappings = mappingDefinition;
    }
    this.mappingService.saveMappingOutput$.subscribe((saveHandler: Function) => {
      const json = this.mappingService.serializeMappingsToJSON(this.cfg.mappings);
      const properties = {
        atlasmapping: JSON.stringify(json),
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
    this.detector.detectChanges();
    log.debugc(() => 'Fetching POM for integration', category);
    this.support.requestPom(this.currentFlow.getIntegrationClone()).subscribe(
      (data) => {
        const pom = data['_body'];
        log.debugc(() => 'Fetched POM for integration: ' + pom, category);
        this.cfg.initCfg.classPath = null;
        this.cfg.initCfg.pomPayload = pom;
        this.initializationService.initialize();
      }, (err) => {
        // do our best I guess
        log.warnc(() => 'failed to fetch pom: ', JSON.parse(err), category);
        this.cfg.initCfg.classPath = '';
        this.initializationService.initialize();
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
