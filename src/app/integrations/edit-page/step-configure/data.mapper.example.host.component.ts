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

import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { FlowPage } from '../flow-page';

const MAPPING_KEY = 'atlasmapping';

@Component({
  selector: 'ipaas-data-mapper-host',
  template: `
    <div *ngIf="cfg.mappings">
      <data-mapper #dataMapperComponent [cfg]="cfg"></data-mapper>
    </div>
  `,
  providers: [ConfigService, MappingManagementService, ErrorHandlerService, DocumentManagementService],
})
export class DataMapperHostComponent extends FlowPage implements OnInit, OnDestroy {

  routeSubscription: Subscription;
  position: number;

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

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integrations-mapper-init':
        const step = this.currentFlow.getStep(this.position);
        let mappings = undefined;
        if (step.configuredProperties && step.configuredProperties[MAPPING_KEY]) {
          try {
            mappings = <any> step.configuredProperties[MAPPING_KEY]['AtlasMapping'];
          } catch (err) {
            // TODO
          }
        }
        this.cfg.mappings = new MappingDefinition();

        const start = this.currentFlow.getStep(this.currentFlow.getFirstPosition());
        const end = this.currentFlow.getStep(this.currentFlow.getLastPosition());
        // TODO we'll want to parse the dataType and maybe set the right config value
        const inputDocDef = new DocumentDefinition();
        inputDocDef.isSource = true;
        inputDocDef.initCfg.documentIdentifier = start.action.outputDataShape['dataType'].replace(/java:/, '');
        this.cfg.sourceDocs.push(inputDocDef);

        const outputDocDef = new DocumentDefinition();
        outputDocDef.isSource = false;
        outputDocDef.initCfg.documentIdentifier = end.action.inputDataShape['dataType'].replace(/java:/, '');
        this.cfg.targetDocs.push(outputDocDef);

        if (mappings) {
          const mappingDefinition = new MappingDefinition();
          // Existing mappings, load from the route
          this.mappingService.deserializeMappingServiceJSON(mappings, mappingDefinition);
          this.cfg.mappings = mappingDefinition;
        }
        this.initializationService.initialize();
        this.mappingService.saveMappingOutput$.subscribe((saveHandler: Function) => {
          const json = this.mappingService.serializeMappingsToJSON(this.cfg.mappings);
          const properties = {
            atlasMapping: json,
          };
          this.currentFlow.events.emit({
            kind: 'integration-set-properties',
            position: this.position,
            properties: properties,
          });
        });
        this.detector.detectChanges();
      break;
    }
  }

  ngOnInit() {
    this.routeSubscription = this.route.params.pluck<Params, string>('position')
      .map((position: string) => {
        this.position = Number.parseInt(position);
        this.currentFlow.events.emit({
          kind: 'integrations-mapper-init',
        });
      })
      .subscribe();
  }

  ngOnDestroy() {
    this.routeSubscription.unsubscribe();
  }


}
