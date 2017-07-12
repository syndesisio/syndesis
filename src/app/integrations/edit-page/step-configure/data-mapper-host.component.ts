import {
  ChangeDetectorRef,
  Component,
  ViewChild,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { DocumentDefinition } from 'syndesis.data.mapper';
import { MappingDefinition } from 'syndesis.data.mapper';
import { ConfigModel } from 'syndesis.data.mapper';
import { MappingModel } from 'syndesis.data.mapper';

import { InitializationService } from 'syndesis.data.mapper';
import { ErrorHandlerService } from 'syndesis.data.mapper';
import { DocumentManagementService } from 'syndesis.data.mapper';
import { MappingManagementService } from 'syndesis.data.mapper';
import { MappingSerializer } from 'syndesis.data.mapper';

import { DataMapperAppComponent } from 'syndesis.data.mapper';

import { ConfigService } from '../../../config.service';
import { IntegrationSupportService } from '../../../store/integration-support.service';

import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { FlowPage } from '../flow-page';

import { Step, DataShape, TypeFactory } from '../../../model';

import { log, getCategory } from '../../../logging';

/*
 * Example host component: 
 *
 * https://github.com/atlasmap/atlasmap-ui/blob/master/src/app/lib/syndesis-data-mapper/components/data.mapper.example.host.component.ts
 */

const category = getCategory('Connections');

const MAPPING_KEY = 'atlasmapping';

@Component({
  selector: 'syndesis-data-mapper-host',
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
  providers: [
    InitializationService,
    ConfigService,
    MappingManagementService,
    ErrorHandlerService,
    DocumentManagementService,
  ],
})
export class DataMapperHostComponent extends FlowPage
  implements OnInit, OnDestroy {
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
    public configService: ConfigService,
    public initializationService: InitializationService,
    public support: IntegrationSupportService,
    public detector: ChangeDetectorRef,
  ) {
    super(currentFlow, route, router, detector);
    this.cfg = initializationService.cfg;

    var baseUrl = "https://syndesis-staging.b6ff.rh-idev.openshiftapps.com/v2/atlas/";
    this.cfg.initCfg.baseJavaInspectionServiceUrl = 
      this.fetchServiceUrl("baseJavaInspectionServiceUrl", baseUrl + "java/", configService);
    this.cfg.initCfg.baseXMLInspectionServiceUrl = 
      this.fetchServiceUrl("baseXMLInspectionServiceUrl", baseUrl + "xml/", configService);
    this.cfg.initCfg.baseJSONInspectionServiceUrl = 
      this.fetchServiceUrl("baseJSONInspectionServiceUrl", baseUrl + "json/", configService);
    this.cfg.initCfg.baseMappingServiceUrl = 
      this.fetchServiceUrl("baseMappingServiceUrl", baseUrl, configService);
  }

  private fetchServiceUrl(configKey: string, defaultUrl: string, configService: ConfigService): string {
    try {
      return configService.getSettings('datamapper', configKey);
    } catch (err) {
      return defaultUrl;
    }
  }

  createDocumentDefinition(dataShape: DataShape, isSource: boolean = false) {
    // TODO: for xml/json docs, we need a document contents
    // reference for document contents: DocumentManagementService.generateMock* methods
    var documentContents: string = null;
    // TODO not sure what to do for `none` or `any` here
    switch (dataShape.kind) {
      case 'java':
        this.cfg.addJavaDocument(dataShape.type, isSource);
        break;
      case 'json':
        this.cfg.addJSONDocument(dataShape.type, documentContents, isSource);
        break;
      case 'xml-instance':
        this.cfg.addXMLInstanceDocument(dataShape.type, documentContents, isSource);
        break;
      case 'xml-instance':
        this.cfg.addXMLSchemaDocument(dataShape.type, documentContents, isSource);
        break;
    }
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

    //TODO: need to loop through document config for multi-source

    // TODO we'll want to parse the dataType and maybe set the right config value
    this.createDocumentDefinition(
      start.action.outputDataShape,
      true,
    );
    this.createDocumentDefinition(
      end.action.inputDataShape,
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
    this.detector.detectChanges();
    log.debugc(() => 'Fetching POM for integration', category);
    this.support.requestPom(this.currentFlow.getIntegrationClone()).subscribe(
      data => {
        const pom = data['_body'];
        log.debugc(() => 'Fetched POM for integration: ' + pom, category);
        this.cfg.initCfg.classPath = null;
        this.cfg.initCfg.pomPayload = pom;
        this.initializationService.initialize();
      },
      err => {
        // do our best I guess
        log.warnc(() => 'failed to fetch pom: ', JSON.parse(err), category);
        this.cfg.initCfg.classPath = '';
        this.initializationService.initialize();
      },
    );
  }

  ngOnInit() {
    this.routeSubscription = this.route.params
      .pluck<Params, string>('position')
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
