import { Component, EventEmitter, Input, Output } from '@angular/core';

import { ObjectPropertySortConfig } from '../../common/object-property-sort.pipe';
import { ObjectPropertyFilterConfig } from '../../common/object-property-filter.pipe';

@Component({
  selector: 'syndesis-integrations-list-toolbar',
  templateUrl: './list-toolbar.component.html',
  styleUrls: ['./list-toolbar.component.scss'],
})
export class IntegrationsListToolbarComponent {

  @Input()
  filter: ObjectPropertyFilterConfig = {
    filter: '',
    propertyName: 'name',
  };
  @Output()
  filterChange = new EventEmitter<ObjectPropertyFilterConfig>();

  @Input()
  sort: ObjectPropertySortConfig = {
    sortField: 'name',
    descending: false ,
  };
  @Output()
  sortChange = new EventEmitter<ObjectPropertySortConfig>();

  @Input()
  showCreate = true;

  constructor() { }

  setPropertyName(propertyName: string) {
    this.filter.propertyName = propertyName;
    this.filterChange.emit(this.filter);
  }

  setSortField(propertyName: string) {
    this.sort.sortField = propertyName;
    this.sortChange.emit(this.sort);
  }

  toggleDescending() {
    this.sort.descending = !this.sort.descending;
    this.sortChange.emit(this.sort);
  }

  filterInputChange(value: string) {
    this.filter.filter = value;
    this.filterChange.emit(this.filter);
  }

}
