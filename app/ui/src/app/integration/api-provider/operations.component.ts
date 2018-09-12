import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { first, map } from 'rxjs/operators';
import { FilterConfig, SortConfig, ToolbarConfig } from 'patternfly-ng';
import {
  ExtensionStore,
  EXTENSION,
  StepStore,
  StepKind, IntegrationStore
} from '../../store/index';
import {
  CurrentFlowService,
  FlowEvent,
  FlowPageService
} from '../edit-page/index';
import { Extensions, Integration, Step, Steps } from '../../platform/index';
import { NavigationService, ObjectPropertyFilterPipe } from '../../common/index';

@Component({
  selector: 'syndesis-integration-api-provider-operations',
  templateUrl: './operations.component.html',
  styleUrls: ['./operations.component.scss']
})
export class IntegrationApiProviderOperationsComponent implements OnInit, OnDestroy {
  integration: Observable<Integration>;
  readonly loading: Observable<boolean>;

  integrationSubscription: Subscription;
  routeSubscription: Subscription;
  childRouteSubscription: Subscription;
  flowSubscription: Subscription;
  fragmentSubscription: Subscription;
  urls: UrlSegment[];
  _canContinue = false;
  position: number;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public integrationStore: IntegrationStore,
    public route: ActivatedRoute,
    public router: Router,
    public navigationService: NavigationService
  ) {
    this.integration = this.integrationStore.resource;
    this.loading = this.integrationStore.loading;
    this.integrationStore.clear();
    this.flowSubscription = this.currentFlowService.events.subscribe(
      (event: FlowEvent) => {
      }
    );
  }
  ngOnInit() {
    this.integrationSubscription = this.integration.subscribe(
      (i: Integration) => {
        if (i) {
          this.currentFlowService.integration = i;
        }
      }
    );

    this.routeSubscription = this.route.paramMap
      .pipe(map(params => {
        return {
          integrationId: params.get('integrationId'),
          flowId: params.get('flowId')
        };
      }))
      .subscribe(params => {
        this.currentFlowService.flowId = params.flowId;
        this.integrationStore.loadOrCreate(params.integrationId);
      });
    this.navigationService.hide();
  }

  ngOnDestroy() {
    this.currentFlowService.flowId = undefined;
    this.navigationService.show();
    if (this.integrationSubscription) {
      this.integrationSubscription.unsubscribe();
    }
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
  }
}
