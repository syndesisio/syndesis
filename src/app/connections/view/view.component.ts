import { Component, EventEmitter, Input, Output, OnDestroy, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { FormGroup } from '@angular/forms';
import { DynamicFormControlModel, DynamicFormService, DynamicInputModel } from '@ng2-dynamic-forms/core';

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
  formChangesSubscription: Subscription;
  configuredProperties: any;

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
    const formFields = this.getFormConfig(this.connection);
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

  getFormConfig(connection: Connection) {
    const config = this.getConfigString(connection);
    if (typeof config === 'string') {
      try {
        return JSON.parse(config);
      } catch (err) {
        log.debugc(() => 'Failed to parse JSON config: ' + err, category);
      }
    } else {
      return config;
    }
  }

  getConfigString(connection: Connection) {
    // TODO this will need adjusting once this is an object
    if (this.connection.configuredProperties) {
      return this.connection.configuredProperties;
    } else if (this.connection.connector && this.connection.connector.properties) {
      return this.connection.connector.properties;
    } else {
      // um...
      return '{}';
    }
  }

  get formModel() {
    if (this._formModel) {
      return this._formModel;
    }
    if (!this.connection) {
      return undefined;
    }
    const config = this.getFormConfig(this.connection);
    if (config) {
      this._formModel = this.formFactory.createFormModel(config);
      return this._formModel;
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
      this.formChangesSubscription = this._formGroup.valueChanges.subscribe((data) => {
        this.connection.configuredProperties = data;
        this.connectionChange.emit(this.connection);
      });
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
    if (this.formChangesSubscription) {
      this.formChangesSubscription.unsubscribe();
    }
  }

}
