import { Component, EventEmitter, Input, Output, OnDestroy, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { ConnectorStore } from '../../store/connector/connector.store';
import { Connection, Connectors, Connector, Tag, TypeFactory } from '../../model';
import { ObjectPropertyFilterConfig } from '../../common/object-property-filter.pipe';
import { ObjectPropertySortConfig } from '../../common/object-property-sort.pipe';

@Component({
  selector: 'ipaas-connection-view',
  templateUrl: './view.component.html',
  styleUrls: ['./view.component.scss'],
})
export class ConnectionViewComponent implements OnInit, OnDestroy {

  @Input() connection: Connection = TypeFactory.createConnection();
  @Output() connectionChange = new EventEmitter<Connection>();
  @Input() mode = 'view';
  connectors: Observable<Connectors>;
  loading: Observable<boolean>;
  filter: ObjectPropertyFilterConfig = {
    filter: '',
    propertyName: 'name',
  };
  sort: ObjectPropertySortConfig = {
    sortField: 'name',
    descending: false,
  };

  constructor(
    private store: ConnectorStore,
  ) {
    this.loading = store.loading;
    this.connectors = store.list;
  }

  get name(): string {
    const name = this.connection.name || '';
    return name;
  }

  set name(name: string) {
    this.connection.name = name;
    this.connectionChange.emit(this.connection);
  }

  get description(): string {
    return this.connection.description;
  }

  set description(description: string) {
    this.connection.description = description;
    this.connectionChange.emit(this.connection);
  }

  get tagsArray(): string[] {
    return (this.connection.tags || []).map((tag) => tag.name);
  }

  get tags(): string {
    return this.tagsArray.join(', ');
  }

  set tags(tags: string) {
    this.connection.tags = tags.split(',').map((str) => <Tag> { name: str.trim() });
    this.connectionChange.emit(this.connection);
  }

  columnClass(): string {
    switch (this.mode) {
      case 'view':
      case 'edit':
        return 'col-xs-8';
      case 'create':
        return 'col-md-12';
      default:
        return 'col-xs-8';
    }
  }

  onSelected(connector: Connector) {
    this.connection.connector = connector;
    this.connectionChange.emit(this.connection);
  }

  getFormFields(connection: Connection) {
    const answer = [];
    let formFields = undefined;
    try {
      formFields = JSON.parse(connection.configuredProperties);
    } catch (err) {
      // silently fail
    }
    if (formFields) {
      for (const key in formFields) {
        if (!formFields.hasOwnProperty(key)) {
          continue;
        }
        const field = formFields[key];
        field.name = key;
        answer.push(field);
      }
    }
    return answer;
  }

  ngOnInit() {
    switch (this.mode) {
      case 'create':
        this.store.loadAll();
      break;
    }

  }

  ngOnDestroy() {

  }

}
