import { ChangeDetectorRef, Component, ViewChild, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { DocumentDefinition } from 'ipaas.data.mapper';
import { MappingDefinition } from 'ipaas.data.mapper';
import { ConfigModel } from 'ipaas.data.mapper';
import { MappingModel } from 'ipaas.data.mapper';

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
    <div *ngIf="cfg.mappingInputJavaClass && cfg.mappingOutputJavaClass && cfg.mappings">
      <data-mapper #dataMapperComponent [cfg]="cfg"></data-mapper>
    </div>
  `,
  providers: [ConfigService],
})
export class DataMapperHostComponent extends FlowPage implements OnInit, OnDestroy {

  routeSubscription: Subscription;
  position: number;

  @ViewChild('dataMapperComponent')
  public dataMapperComponent: DataMapperAppComponent;

  public cfg: ConfigModel = {
    // TODO fetch these from the server
    baseJavaServiceUrl: 'https://ipaas-staging.b6ff.rh-idev.openshiftapps.com/v2/atlas/java/',
    baseMappingServiceUrl: 'https://ipaas-staging.b6ff.rh-idev.openshiftapps.com/v2/atlas/',
    mappingInputJavaClass: undefined,
    mappingOutputJavaClass: undefined,
    mappings: undefined,
    documentService: undefined,
    mappingService: undefined,
    errorService: undefined,
    showMappingDetailTray: false,
    showMappingDataType: false,
    showLinesAlways: false,
    inputDoc: undefined,
    outputDoc: undefined,
  };

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public documentService: DocumentManagementService,
    public mappingService: MappingManagementService,
    public errorService: ErrorHandlerService,
    public configService: ConfigService,
    public detector: ChangeDetectorRef,
  ) {
    super(currentFlow, route, router);
    try {
      this.cfg.baseJavaServiceUrl = configService.getSettings('datamapper', 'baseJavaServiceUrl') || this.cfg.baseJavaServiceUrl;
      this.cfg.baseMappingServiceUrl = configService.getSettings('datamapper', 'baseMappingServiceUrl') || this.cfg.baseMappingServiceUrl;
    } catch (err) {
      // run with defaults
    }
  }


  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integrations-mapper-init':
        const step = this.currentFlow.getStep(this.position);
        let mappings = undefined;
        if (step.configuredProperties && step.configuredProperties[MAPPING_KEY]) {
          try {
            mappings = <any> JSON.parse(step.configuredProperties[MAPPING_KEY])['AtlasMapping'];
          } catch (err) {
            // TODO
          }
        }
        this.cfg.mappings = new MappingDefinition();
        if (!mappings) {
          const start = this.currentFlow.getStep(this.currentFlow.getFirstPosition());
          const end = this.currentFlow.getStep(this.currentFlow.getLastPosition());
          // TODO we'll want to parse the dataType and maybe set the right config value
          this.cfg.mappingInputJavaClass = start.action.outputDataShape['dataType'].replace(/java:/, '');
          this.cfg.mappingOutputJavaClass = end.action.inputDataShape['dataType'].replace(/java:/, '');
        } else {
          // TODO probably a better way to handle these URIs
          this.cfg.mappingInputJavaClass = (<string>mappings['sourceUri']).replace(/atlas:java\?className=/, '');
          this.cfg.mappingOutputJavaClass = (<string>mappings['targetUri']).replace(/atlas:java\?className=/, '');
        }
        this.documentService.cfg = this.cfg;
        this.mappingService.cfg = this.cfg;
        this.cfg.documentService = this.documentService;
        this.cfg.mappingService = this.mappingService;
        this.cfg.errorService = this.errorService;
        this.documentService.initialize();
        this.mappingService.initialize();
        this.mappingService.saveMappingOutput$.subscribe((saveHandler: Function) => {
          this.mappingService.saveMappingToService(saveHandler);
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
