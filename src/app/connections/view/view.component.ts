import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  Output,
  OnChanges,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService,
  DynamicInputModel,
} from '@ng2-dynamic-forms/core';

import { FormFactoryService } from '../../common/forms.service';
import { ConnectionService } from '../../store/connection/connection.service';
import { ConnectorStore } from '../../store/connector/connector.store';
import { Connection, Connectors, Connector, TypeFactory } from '../../model';
import { log, getCategory } from '../../logging';

const category = getCategory('Connections');

@Component({
  selector: 'syndesis-connection-view',
  templateUrl: './view.component.html',
  styleUrls: ['./view.component.scss'],
})
export class ConnectionViewComponent implements OnInit, OnChanges, OnDestroy {
  @Input() connection: Connection = TypeFactory.createConnection();
  @Output() connectionChange = new EventEmitter<Connection>();
  @Input() mode = 'view';
  @Input() showName = true;
  @Input() inView = false;
  validating = false;
  validateError: string = undefined;
  validateSuccess = false;
  loading: Observable<boolean>;
  connectors: Observable<Connectors>;
  filteredConnectors: Subject<Connectors> = new BehaviorSubject(<Connectors>{});
  _formModel: DynamicFormControlModel[];
  _formGroup: FormGroup;
  formChangesSubscription: Subscription;
  configuredProperties: any;
  nameErrorMessage: string = null;

  constructor(
    private connectionService: ConnectionService,
    private store: ConnectorStore,
    private formFactory: FormFactoryService,
    private formService: DynamicFormService,
    private detector: ChangeDetectorRef,
  ) {
    this.loading = store.loading;
    this.connectors = store.list;
  }

  showButton(id: string) {
    switch (id) {
      case 'salesforce':
      case 'twitter':
        return true;
    }
    return false;
  }

  doValidate(connector: Connector, formGroup: FormGroup) {
    this.validateSuccess = false;
    this.validateError = undefined;
    this.validating = true;
    const data = formGroup.value;
    const sanitized: any = {};
    // last minute sanitization, strip out any null/empty values
    for (const key of Object.keys(data)) {
      const trimmed = key.trim();
      const value = data[key] || '';
      sanitized[trimmed] = value === '' ? undefined : value;
    }
    this.store.validate(connector.id, sanitized).subscribe(
      resp => {
        setTimeout(() => {
          this.validating = false;
          let errorHit = false;
          (<Array<any>>resp).forEach(info => {
            if (!errorHit) {
              if (info['status'] === 'ERROR') {
                errorHit = true;
                this.validateError = (<Array<any>>info)['errors']
                  .map(err => {
                    return err['description'];
                  })
                  .join(', \n');
              }
            }
          });
          if (!errorHit) {
            this.validateSuccess = true;
          }
          this.detector.detectChanges();
        }, 10);
      },
      err => {
        setTimeout(() => {
          this.validateError = err.message ? err.message : err;
          this.validating = false;
          this.detector.detectChanges();
        }, 10);
      },
    );
  }

  validateName() {
    if (!this.connection.name || this.connection.name.trim() === '') {
      this.nameErrorMessage = 'Name is required';
    } else {
      this.connectionService.validate(this.connection)
      .toPromise()
      .then(response => null)
      .catch(response => {
        const nameTaken = response.data.filter(item => item.error === 'UniqueProperty').length > 0;
        this.nameErrorMessage = nameTaken ? 'That name is taken. Try another.' : null;
        this.detector.detectChanges();
      });
    }
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

  get tags(): string[] {
    return this.connection.tags;
  }

  set tags(tags: string[]) {
    this.connection.tags = tags;
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
        field.value = this.connection.configuredProperties[key];
        answer.push(field);
      }
    }
    return answer;
  }

  getPassword(value: any) {
    return Array(10).join('*');
  }

  getFormConfig(connection: Connection) {
    // TODO this either shouldn't be null or we need to just fetch the connector in a separate call
    if (connection.connector) {
      const props = JSON.parse(JSON.stringify(connection.connector.properties));
      if (connection.configuredProperties) {
        Object.keys(connection.configuredProperties).forEach(key => {
          if (props[key]) {
            props[key].value = connection.configuredProperties[key];
          }
        });
      } else {
        connection.configuredProperties = {};
      }
      return props;
    }
    return {};
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
      this.formChangesSubscription = this._formGroup.valueChanges.subscribe(
        data => {
          Object.keys(data).forEach(key => {
            if (data[key] === null) {
              delete data[key];
            }
          });
          this.connection.configuredProperties = data;
          this.connectionChange.emit(this.connection);
        },
      );
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
      case 'create1':
        this.store.loadAll();
        break;
      default:
        // nothing to do
        break;
    }
  }

  ngOnChanges(changes: any) {
    if (changes.connection) {
      delete this._formGroup;
      delete this._formModel;
    }
  }

  ngOnDestroy() {
    if (this.formChangesSubscription) {
      this.formChangesSubscription.unsubscribe();
    }
  }
}
