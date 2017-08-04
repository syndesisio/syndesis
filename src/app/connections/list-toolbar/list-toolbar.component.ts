import { Component, EventEmitter, Input, Output, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import {
  ActionConfig,
  FilterConfig,
  FilterEvent,
  SortConfig,
  SortField,
  SortEvent,
  ToolbarConfig,
} from 'patternfly-ng';

import { ObjectPropertyFilterPipe } from '../../common/object-property-filter.pipe';
import { ObjectPropertySortPipe } from '../../common/object-property-sort.pipe';

@Component({
  selector: 'syndesis-connections-list-toolbar',
  templateUrl: './list-toolbar.component.html',
  styleUrls: ['./list-toolbar.component.scss'],
})
export class ConnectionsListToolbarComponent<T> implements OnInit, OnDestroy {

  @Input() showCreate = true;
  @Input() items: Observable<Array<T>> = Observable.empty();
  @Input() filteredItems: Subject<Array<T>>;
  toolbarConfig: ToolbarConfig;
  private _allItems: Array<T> = [];
  private _filteredItems: Array<T> = [];
  private subscription: Subscription;
  private currentSortFieldId: string;
  private isAscendingSort = true;
  private propertyFilter = new ObjectPropertyFilterPipe();
  private propertySorter = new ObjectPropertySortPipe();

  ngOnInit() {
    const filterConfig = {
      fields : [{
        id          : 'name',
        title       : 'Name',
        placeholder : 'Filter by Name...',
        type        : 'text',
      }],
      appliedFilters : [],
    };
    const sortConfig = {
      fields : [{
        id       : 'name',
        title    : 'Name',
        sortType : 'alpha',
      }],
      isAscending : this.isAscendingSort,
    } as SortConfig;

    this.toolbarConfig = {
      filterConfig : filterConfig,
      sortConfig   : sortConfig,
    } as ToolbarConfig;

    this.subscription = this.items
      .do(connections => this._allItems = connections)
      .do(_ => this.filter())
      .subscribe();
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  filter(): void {
     const result = this.toolbarConfig.filterConfig.appliedFilters
      .reduce((items, filter) => this.propertyFilter.transform(items, {
          filter       : filter.value,
          propertyName : filter.field.id,
        }), this._allItems);
    this.toolbarConfig.filterConfig.resultsCount = result.length;
    this._filteredItems = result;
    this.sort();
  }

  sort($event?: SortEvent): void {
    if ($event) {
      this.currentSortFieldId = $event.field.id;
      this.isAscendingSort = $event.isAscending;
    }
    const result = this.propertySorter.transform(this._filteredItems, {
      sortField  : this.currentSortFieldId || 'name',
      descending : !this.isAscendingSort,
    });
    this.filteredItems.next(result);
  }
}
