import { Component, EventEmitter, OnInit, Input, Output } from '@angular/core';

import { ObjectPropertySortConfig } from '../../common/object-property-sort.pipe';
import { ObjectPropertyFilterConfig } from '../../common/object-property-filter.pipe';

export interface ListToolbarProperties {
  key: string;
  value: string;
}

@Component({
  selector: 'syndesis-list-toolbar',
  templateUrl: './list-toolbar.component.html',
  styleUrls: ['./list-toolbar.component.scss'],
})
export class ListToolbarComponent implements OnInit {
  @Input()
  properties: Array<ListToolbarProperties> = [];

  @Input()
  filter: ObjectPropertyFilterConfig = {
    filter: '',
    propertyName: 'name',
  };
  @Output() filterChange = new EventEmitter<ObjectPropertyFilterConfig>();

  @Input()
  sort: ObjectPropertySortConfig = {
    sortField: 'name',
    descending: false,
  };
  @Output() sortChange = new EventEmitter<ObjectPropertySortConfig>();

  @Input() showCreate = true;

  constructor() {}

  setPropertyName(propertyName: string) {
    if (!this.filter) {
      this.filter = {
        filter: '',
        propertyName: propertyName,
      };
    } else {
      this.filter.propertyName = propertyName;
    }
    this.filterChange.emit(this.filter);
  }

  setSortField(propertyName: string) {
    if (!this.sort) {
      this.sort = {
        sortField: propertyName,
        descending: false,
      };
    } else {
      this.sort.sortField = propertyName;
    }
    this.sortChange.emit(this.sort);
  }

  toggleDescending() {
    if (!this.sort) {
      this.sort = {
        sortField: undefined,
        descending: false,
      };
    } else {
      this.sort.descending = !this.sort.descending;
    }
    this.sortChange.emit(this.sort);
  }

  filterInputChange(value: string) {
    if (!this.filter) {
      this.filter = {
        filter: value,
        propertyName: undefined,
      };
    } else {
      this.filter.filter = value;
    }
    this.filterChange.emit(this.filter);
  }

  ngOnInit() {

  }
}
