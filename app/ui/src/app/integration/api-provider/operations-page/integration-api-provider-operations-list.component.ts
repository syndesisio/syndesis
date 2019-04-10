import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { Flow } from '@syndesis/ui/platform';
import {
  ObjectPropertyFilterConfig,
  ObjectPropertySortConfig,
} from '@syndesis/ui/common';
import {
  FilterConfig,
  SortConfig,
  ToolbarConfig,
  ListEvent,
} from 'patternfly-ng';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Router } from '@angular/router';

@Component({
  selector: 'syndesis-integration-api-provider-operations-list',
  templateUrl: './integration-api-provider-operations-list.component.html'
})
export class ApiProviderOperationsListComponent implements OnInit, OnDestroy {
  @Input()
  integrationId: string;
  @Input()
  flows$: Observable<Flow[]>;

  enrichedFlows$ = new BehaviorSubject<Flow[]>(undefined);
  filteredFlows$ = new BehaviorSubject<Flow[]>(undefined);
  filter: ObjectPropertyFilterConfig = {
    filter: '',
    propertyName: 'name',
  };
  sort: ObjectPropertySortConfig = {
    sortField: 'implemented',
    descending: true,
  };

  toolbarConfig = {
    filterConfig: {
      fields: [
        {
          id: 'name',
          title: 'Operation Name',
          placeholder: 'Filter by Operation Name...',
          type: 'text',
        },
        {
          id: 'description',
          title: 'Method & Name',
          placeholder: 'Filter by Method & Name...',
          type: 'text',
        },
      ],
    } as FilterConfig,
    sortConfig: {
      fields: [
        {
          id: 'implemented',
          title: 'Implemented',
          sortType: 'numeric',
        },
        {
          id: 'name',
          title: 'Operation Name',
          sortType: 'alpha',
        },
        {
          id: 'description',
          title: 'Method & Path',
          sortType: 'alpha',
        },
      ],
      isAscending: false,
    } as SortConfig,
  } as ToolbarConfig;
  subscription: any;

  constructor(private router: Router) {}

  ngOnInit() {
    this.subscription = this.flows$
      .pipe(
        map(flows => {
          return flows
            .map(f => {
              return {
                ...f,
                implemented: f.metadata.excerpt.startsWith('501') ? 0 : 1,
              };
            })
            .sort((a, b) => b.implemented - a.implemented);
        })
      )
      .subscribe(flows => this.enrichedFlows$.next(flows));
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  handleClick($event: ListEvent | Flow): void {
    const item = ($event as ListEvent).item || ($event as Flow);
    this.router.navigate([
      'integrations',
      this.integrationId,
      'operations',
      (item as Flow).id,
      'edit',
    ]);
  }
}
