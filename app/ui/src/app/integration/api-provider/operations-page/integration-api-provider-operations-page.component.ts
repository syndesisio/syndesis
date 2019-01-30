import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subscription, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  FilterConfig,
  ListEvent,
  SortConfig,
  ToolbarConfig
} from 'patternfly-ng';
import { IntegrationStore } from '@syndesis/ui/store';
import {
  CurrentFlowService,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';
import { Flow, Integration, Flows } from '@syndesis/ui/platform';
import {
  NavigationService,
  ObjectPropertyFilterConfig,
  ObjectPropertySortConfig
} from '@syndesis/ui/common';

@Component({
  selector: 'syndesis-integration-api-provider-operations',
  templateUrl: './integration-api-provider-operations-page.component.html',
  styleUrls: [
    '../../integration-common.scss',
    './integration-api-provider-operations-page.component.scss'
  ]
})
export class ApiProviderOperationsComponent implements OnInit, OnDestroy {
  integration: Observable<Integration>;
  readonly loading: Observable<boolean>;

  flows$ = new BehaviorSubject<Flows>([]);
  filteredFlows$ = new BehaviorSubject<Flows>([]);

  integrationSubscription: Subscription;
  routeSubscription: Subscription;

  filter: ObjectPropertyFilterConfig = {
    filter: '',
    propertyName: 'name'
  };
  sort: ObjectPropertySortConfig = {
    sortField: 'name',
    descending: false
  };

  toolbarConfig = {
    filterConfig: {
      fields: [
        {
          id: 'name',
          title: 'Operation Name',
          placeholder: 'Filter by Operation Name...',
          type: 'text'
        },
        {
          id: 'description',
          title: 'Method & Name',
          placeholder: 'Filter by Method & Name...',
          type: 'text'
        }
      ]
    } as FilterConfig,
    sortConfig: {
      fields: [
        {
          id: 'implemented',
          title: 'Implemented',
          sortType: 'numeric'
        },
        {
          id: 'name',
          title: 'Operation Name',
          sortType: 'alpha'
        },
        {
          id: 'description',
          title: 'Method & Path',
          sortType: 'alpha'
        }
      ],
      isAscending: false
    } as SortConfig
  } as ToolbarConfig;

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
  }

  ngOnInit() {
    this.integrationSubscription = this.integration.subscribe(
      (i: Integration) => {
        if (i) {
          this.currentFlowService.integration = i;
          this.flows$.next(
            this.currentFlowService.flows.map(flow => {
              return {
                ...flow,
                implemented: flow.metadata.excerpt.startsWith('501') ? 0 : 1
              };
            })
          );
        }
      }
    );

    this.routeSubscription = this.route.paramMap
      .pipe(
        map(params => {
          return {
            integrationId: params.get('integrationId'),
            flowId: params.get('flowId')
          };
        })
      )
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
  }

  handleClick($event: ListEvent | Flow): void {
    const item = ($event as ListEvent).item || ($event as Flow);
    this.router.navigate([
      'integrations',
      this.currentFlowService.integration.id,
      (item as Flow).id,
      'edit'
    ]);
  }
}
