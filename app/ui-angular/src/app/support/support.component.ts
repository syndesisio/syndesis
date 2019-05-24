import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import * as fileSaver from 'file-saver';

import {
  IntegrationSupportService,
  ApiHttpService
} from '@syndesis/ui/platform';
import { log } from '@syndesis/ui/logging';
import { IntegrationStore } from '@syndesis/ui/store';

import {
  PaginationConfig,
  PaginationEvent,
  ToolbarConfig,
  FilterConfig,
  SortConfig,
  SortField,
  SortEvent,
  ListConfig,
  Filter,
  FilterEvent,
  FilterType,
  FilterField,
  NotificationType
} from 'patternfly-ng';

const ARCHIVE_FILE_NAME = 'syndesis.zip';

@Component({
  selector: 'syndesis-support',
  templateUrl: './support.component.html',
  styleUrls: ['./support.component.scss']
})
export class SupportComponent implements OnInit {
  allLogsSelected = true;
  loading = true;
  saving = false;

  // List configuration
  listConfig = this.obtainListConfig();

  // Toolbar configuration
  toolbarConfig = this.obtainToolbarConfig();

  filtersText = '';
  filterConfig: FilterConfig;

  items: Array<any>; // = [{id: "aaaa", name: "AAAA"},{id: "bbb", name: "bbb"}];

  itemsInPage: Array<any> = [];

  currentSortField: SortField;
  isAscendingSort = true;

  paginationConfig: PaginationConfig;

  notificationType: NotificationType = NotificationType.DANGER;
  notificationHidden = true;

  version$: Observable<any>;

  constructor(
    public store: IntegrationStore,
    public integrationSupportService: IntegrationSupportService,
    private apiHttpService: ApiHttpService
  ) {}

  buildData(data: any = {}): void {
    this.saving = true;
    this.integrationSupportService.downloadSupportData(data).subscribe(
      response => {
        fileSaver.saveAs(response, ARCHIVE_FILE_NAME);
        this.saving = false;
      },
      error => {
        log.error('Error downloading file', error);
        this.notificationHidden = false;
        this.saving = false;
      }
    );
  }

  // UI event handling

  // Handle filter changes
  filterChanged($event: FilterEvent): void {
    this.filtersText = '';
    $event.appliedFilters.forEach(filter => {
      this.filtersText += filter.field.title + ' : ' + filter.value + '\n';
    });
    this.applyFilters($event.appliedFilters);
    this.filterFieldSelected($event);
  }

  // Reset filtered queries
  filterFieldSelected($event: FilterEvent): void {
    if ($event.appliedFilters.length === 0) {
      this.updateItems();
    }
  }

  // Filter
  applyFilters(filters: Filter[]): void {
    this.itemsInPage = [];
    if (filters && filters.length > 0) {
      this.items.forEach(item => {
        if (this.matchesFilters(item, filters)) {
          this.itemsInPage.push(item);
        }
      });
    } else {
      this.itemsInPage = this.items;
    }
    this.toolbarConfig.filterConfig.resultsCount = this.itemsInPage.length;
  }

  matchesFilters(item: any, filters: Filter[]): boolean {
    let matches = true;
    filters.forEach(filter => {
      if (!this.matchesFilter(item, filter)) {
        matches = false;
        return matches;
      }
    });
    return matches;
  }

  matchesFilter(item: any, filter: Filter): boolean {
    let match = true;
    if (filter.field.id === 'name') {
      match = item.name.match(filter.value) !== null;
    } else if (filter.field.id === 'description') {
      match = item.description.match(filter.value) !== null;
    }
    return match;
  }

  // Handle sort changes
  sortChanged($event: SortEvent): void {
    this.currentSortField = $event.field;
    this.isAscendingSort = $event.isAscending;
    this.itemsInPage.sort((item1: any, item2: any) => {
      let compValue = item1[this.currentSortField.id].localeCompare(
        item2[this.currentSortField.id]
      );
      if (!this.isAscendingSort) {
        compValue = compValue * -1;
      }
      return compValue;
    });
  }

  onSubmit(): void {
    let chosen = [];
    if (this.allLogsSelected) {
      chosen = this.items;
    } else {
      chosen = this.itemsInPage.filter(x => x.selected === true);
    }

    const input = {};
    chosen.forEach(el => (input[el.name] = true));
    this.buildData(input);
  }

  deselectAll(): void {
    this.items.forEach(item => (item.selected = false));
  }

  handleSelectionChange(event): void {
    this.allLogsSelected = false;
  }

  totalItems(): number {
    return this.items.length;
  }

  selectedItems(): number {
    return this.itemsInPage.filter(x => x.selected === true).length;
  }

  selectAllMatchingFiler(pfnglist): void {
    this.items
      .filter(x => pfnglist.items.includes(x))
      .forEach(item => (item.selected = true));
  }

  handlePageSize($event: PaginationEvent) {
    this.paginationConfig.pageSize = $event.pageSize;
    this.updateItems();
  }

  handlePageNumber($event: PaginationEvent) {
    this.paginationConfig.pageNumber = $event.pageNumber;
    this.updateItems();
  }

  updateItems() {
    this.itemsInPage = this.items
      .slice(
        (this.paginationConfig.pageNumber - 1) * this.paginationConfig.pageSize,
        this.paginationConfig.totalItems
      )
      .slice(0, this.paginationConfig.pageSize);
  }

  obtainToolbarConfig(): ToolbarConfig {
    return {
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
            title: 'Description',
            placeholder: 'Filter by Description...',
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
            title: 'Description',
            sortType: 'alpha'
          }
        ],
        isAscending: true
      } as SortConfig
    } as ToolbarConfig;
  }

  obtainListConfig(): ListConfig {
    return {
      multiSelect: true,
      selectItems: false,
      showCheckbox: true
    } as ListConfig;
  }

  obtainFilterConfig(): FilterConfig {
    return {
      fields: [
        {
          id: 'name',
          title: 'Name',
          placeholder: 'Filter by Name...',
          type: FilterType.TEXT
        },
        {
          id: 'description',
          title: 'Description',
          placeholder: 'Filter by Description...',
          type: FilterType.TEXT
        }
      ] as FilterField[],
      resultsCount: this.items.length,
      appliedFilters: []
    } as FilterConfig;
  }

  obtainPaginationConfig(): PaginationConfig {
    return {
      pageSize: 5,
      pageNumber: 1,
      totalItems: this.items.length
    } as PaginationConfig;
  }

  public ngOnInit() {
    this.store.list.subscribe(integrations => {
      this.items = integrations;
      this.paginationConfig = this.obtainPaginationConfig();
      this.filterConfig = this.obtainFilterConfig();
      this.updateItems();
    });
    this.store.loadAll();
    this.version$ = this.apiHttpService.get('/version', {
      headers: 'Accept: application/json'
    });
  }
}
