import { Component, EventEmitter, Input, Output, OnDestroy, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { FormGroup } from '@angular/forms';
import { DynamicFormControlModel, DynamicFormService } from '@ng2-dynamic-forms/core';

import { FormFactoryService } from '../../common/forms.service';
import { ConnectorStore } from '../../store/connector/connector.store';
import { Connection, Connectors, Connector, Tag, TypeFactory } from '../../model';
import { ObjectPropertyFilterConfig } from '../../common/object-property-filter.pipe';
import { ObjectPropertySortConfig } from '../../common/object-property-sort.pipe';
import { log, getCategory } from '../../logging';

const category = getCategory('Connections');

@Component({
  selector: 'ipaas-connection-view',
  templateUrl: './view.component.html',
  styleUrls: ['./view.component.scss'],
})
export class ConnectionViewComponent implements OnInit, OnDestroy {

  @Input() connection: Connection = TypeFactory.createConnection();
  @Output() connectionChange = new EventEmitter<Connection>();
  @Input() mode = 'view';
  @Input() showName = true;
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
  _formModel: DynamicFormControlModel[];
  _formGroup: FormGroup;

  constructor(
    private store: ConnectorStore,
    private formFactory: FormFactoryService,
    private formService: DynamicFormService,
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
    return this.showName ? 'col-xs-8' : 'col-md-12';
  }

  onSelected(connector: Connector) {
    // TODO maybe a helper function for this...
    const plain = connector['plain'];
    if (plain && typeof plain === 'function') {
      this.connection.connector = plain();
    } else {
      this.connection.connector = connector;
    }
    this.connection.icon = connector.icon;
    this.connection.connectorId = connector.id;
    this.connectionChange.emit(this.connection);
  }

  getFormFields(connection: Connection) {
    const answer = [];
    let formFields = undefined;
    try {
      formFields = JSON.parse(this.getConfigString(this.connection));
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

  getConfigString(connection: Connection) {
    // TODO this will need adjusting once this is an object
    if (this.connection.connector) {
      return this.connection.connector.properties;
    } else {
      return this.connection.configuredProperties;
    }
  }

  get formModel() {
    if (this._formModel) {
      return this._formModel;
    }
    if (!this.connection) {
      return undefined;
    }
    const configString = this.getConfigString(this.connection);
    if (configString) {
      try {
        const formConfig = JSON.parse(configString);
        log.debugc(() => 'Form config: ' + JSON.stringify(formConfig, undefined, 2), category);
        this._formModel = this.formFactory.createFormModel(formConfig);
        return this._formModel;
      } catch (err) {
        log.debugc(() => 'Error parsing form config', category);
      }
    }
    return undefined;
  }

  get formGroup() {
    if (this._formGroup) {
      return this._formGroup;
    }
    const formModel = this.formModel;
    if (formModel) {
      this._formGroup = this.formService.createFormGroup(formModel);
      return this._formGroup;
    } else {
      return undefined;
    }
  }

  set formModel(formModel: DynamicFormControlModel[]) {
    this._formModel = formModel;
  }

  set formGroup(formGroup: FormGroup) {
    this._formGroup = formGroup;
  }

  ngOnInit() {
    switch (this.mode) {
      case 'create':
        this.store.loadAll();
      break;
      case 'edit':
      break;
    }

  }

  ngOnDestroy() {

  }

}
