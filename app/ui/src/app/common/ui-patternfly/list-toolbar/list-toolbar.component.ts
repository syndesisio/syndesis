import { EMPTY, Subject, Observable, Subscription } from 'rxjs';

import { tap } from 'rxjs/operators';
import {
  Component,
  Input,
  OnDestroy,
  OnInit,
  TemplateRef
} from '@angular/core';

import {
  FilterConfig,
  FilterEvent,
  FilterField,
  SortConfig,
  SortEvent,
  ToolbarConfig
} from 'patternfly-ng';

import { ObjectPropertyFilterPipe } from '@syndesis/ui/common/object-property-filter.pipe';
import { ObjectPropertySortPipe } from '@syndesis/ui/common/object-property-sort.pipe';

@Component({
  selector: 'syndesis-list-toolbar',
  templateUrl: './list-toolbar.component.html',
  styleUrls: ['./list-toolbar.component.scss']
})
export class ListToolbarComponent<T> implements OnInit, OnDestroy {
  @Input() items: Observable<Array<T>> = EMPTY;
  @Input() filteredItems: Subject<Array<T>>;
  @Input() actionTemplate: TemplateRef<any>;
  @Input() viewTemplate: TemplateRef<any>;
  @Input() filterFields: Array<FilterField> = [];
  @Input() filterTags: true;
  @Input() toolbarConfig: ToolbarConfig;

  private allItems: Array<T> = [];
  private itemsFiltered: Array<T> = [];
  private subscription: Subscription;
  private currentSortFieldId: string;
  private isAscendingSort = true;
  private propertyFilter = new ObjectPropertyFilterPipe();
  private propertySorter = new ObjectPropertySortPipe();

  ngOnInit() {
    if (!this.toolbarConfig) {
      const filterConfig = {
        fields: [
          {
            id: 'name',
            title: 'Name',
            placeholder: 'Filter by Name...',
            type: 'text'
          }
        ],
        appliedFilters: []
      } as FilterConfig;
      filterConfig.fields.push(...this.filterFields);
      const sortConfig = {
        fields: [
          {
            id: 'name',
            title: 'Name',
            sortType: 'alpha'
          }
        ],
        isAscending: this.isAscendingSort
      } as SortConfig;

      this.toolbarConfig = {
        filterConfig: filterConfig,
        sortConfig: sortConfig
      } as ToolbarConfig;
    }
    if (!this.toolbarConfig.filterConfig.appliedFilters) {
      this.toolbarConfig.filterConfig.appliedFilters = [];
    }

    this.subscription = this.items
      .pipe(
        tap(items => (this.allItems = items)),
        tap(items => {
          if (!this.filterTags) {
            return;
          }
          if (items.find(item => item['tags'])) {
            if (
              !this.toolbarConfig.filterConfig.fields.find(
                field => field.id === 'tag'
              )
            ) {
              this.toolbarConfig.filterConfig.fields.push({
                id: 'tag',
                title: 'Tag',
                placeholder: 'Filter by tag...',
                type: 'typeahead'
              });
            }
          } else {
            const index = this.toolbarConfig.filterConfig.fields.findIndex(
              field => field.id === 'tag'
            );
            if (index >= 0) {
              this.toolbarConfig.filterConfig.fields.splice(index, 1);
            }
          }
        }),
        tap(_ => this.filter())
      )
      .subscribe();
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  filter(): void {
    const result =
      this.toolbarConfig.filterConfig.appliedFilters.reduce(
        (items, filter) =>
          filter.field.id === 'tag'
            ? items.filter(item =>
                Array.isArray(item['tags'])
                  ? item['tags'].some(tag => tag === filter.query.value)
                  : false
              )
            : this.propertyFilter.transform(items, {
                filter: filter.value,
                propertyName: filter.field.id,
                exact: filter.field.type !== 'text'
              }),
        this.allItems
      ) || [];
    this.toolbarConfig.filterConfig.resultsCount = result.length;
    this.itemsFiltered = result;
    this.sort();
  }

  sort($event?: SortEvent): void {
    if ($event) {
      this.currentSortFieldId = $event.field.id;
      this.isAscendingSort = $event.isAscending;
    }
    if (!this.currentSortFieldId) {
      this.currentSortFieldId = this.toolbarConfig.sortConfig.fields[0].id;
      this.isAscendingSort = this.toolbarConfig.sortConfig.isAscending;
    }
    const result = this.propertySorter.transform(this.itemsFiltered, {
      sortField: this.currentSortFieldId || 'name',
      descending: !this.isAscendingSort
    });
    this.filteredItems.next(result);
  }

  filterFieldSelected($event: FilterEvent) {
    const field = $event.field;
    if (field.id === 'tag') {
      field.queries = this.allItems
        .map(item => item['tags'] || [])
        .reduce((array, tags) => array.concat(tags), [])
        .filter((tag, i, tags) => tags.indexOf(tag) === i)
        .map(tag => ({ id: tag, value: tag }));
    }
  }
}
