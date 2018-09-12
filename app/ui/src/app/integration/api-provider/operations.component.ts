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
import { NavigationService, ObjectPropertyFilterConfig, ObjectPropertyFilterPipe, ObjectPropertySortConfig } from '../../common/index';

@Component({
  selector: 'syndesis-integration-api-provider-operations',
  templateUrl: './operations.component.html',
  styleUrls: ['../integration-common.scss', './operations.component.scss']
})
export class IntegrationApiProviderOperationsComponent implements OnInit, OnDestroy {
  integration: Observable<Integration>;
  readonly loading: Observable<boolean>;

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

  // Handles events when the user interacts with the toolbar filter
  filterChanged($event) {
    // TODO update our pipe to handle multiple filters
    if ($event.appliedFilters.length === 0) {
      this.filter.filter = '';
    }
    $event.appliedFilters.forEach(filter => {
      this.filter.propertyName = filter.field.id;
      this.filter.filter = filter.value;
    });
  }

  // Handles events when the user interacts with the toolbar sort
  sortChanged($event) {
    this.sort.sortField = $event.field.id;
    this.sort.descending = !$event.isAscending;
  }
}
