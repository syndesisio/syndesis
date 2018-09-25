import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subscription, BehaviorSubject } from 'rxjs';
import { map } from 'rxjs/operators';
import { FilterConfig, ListEvent, SortConfig, ToolbarConfig } from 'patternfly-ng';
import {
  IntegrationStore
} from '@syndesis/ui/store';
import {
  CurrentFlowService,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';
import { Flow, Integration, Flows } from '@syndesis/ui/platform';
import { NavigationService, ObjectPropertyFilterConfig, ObjectPropertySortConfig } from '@syndesis/ui/common';

@Component({
  selector: 'syndesis-integration-api-provider-operations',
  templateUrl: './integration-api-provider-operations-page.component.html',
  styleUrls: ['../../integration-common.scss', './integration-api-provider-operations-page.component.scss']
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
          title: 'Name',
          placeholder: 'Filter by Name...',
          type: 'text'
        },
        {
          id: 'description',
          title: 'Url',
          placeholder: 'Filter by Url...',
          type: 'text'
        }
      ]
    } as FilterConfig,
    sortConfig: {
      fields: [
        {
          id: 'name',
          title: 'Name',
          sortType: 'alpha'
        },
        {
          id: 'description',
          title: 'Url',
          sortType: 'alpha'
        }
      ],
      isAscending: true
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
          this.flows$.next(this.currentFlowService.flows);
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
  }

  handleClick($event: ListEvent): void {
    this.router.navigate([
      'integrations',
      this.currentFlowService.integration.id,
      ($event.item as Flow).id,
      'edit'
    ]);
  }
}
