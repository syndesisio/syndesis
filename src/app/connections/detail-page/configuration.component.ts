import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  Output,
  OnChanges,
  SimpleChanges,
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

import { ConnectionService } from '../../store/connection/connection.service';
import { ConnectorStore } from '../../store/connector/connector.store';
import { Connection, Connectors, Connector, TypeFactory } from '../../model';
import { log, getCategory } from '../../logging';
import { ConnectionDetailConfigurationService } from './configuration.service';

const category = getCategory('Connections');

@Component({
  selector: 'syndesis-connection-detail-configuration',
  templateUrl: './configuration.component.html',
  styles: [`
    .alert { margin-top: 15px; margin-bottom: 0; }
  `],
})
export class ConnectionDetailConfigurationComponent implements OnChanges {

  @Input() connection: Connection;
  @Output() updated = new EventEmitter<Connection>();
  mode: 'view' | 'edit' = 'view';
  formModel: DynamicFormControlModel[];
  formGroup: FormGroup;
  validating: boolean;
  validateError: string;
  validateSuccess: boolean;

  constructor(
    private configurationService: ConnectionDetailConfigurationService,
    private connectorStore: ConnectorStore,
    private formService: DynamicFormService,
    private detector: ChangeDetectorRef,
  ) { }

  ngOnChanges(changes: SimpleChanges): void {
    this.resetView(true);
  }

  edit() {
    this.mode = 'edit';
    this.resetView(false);
  }

  cancel() {
    this.mode = 'view';
    this.resetView(true);
  }

  save() {
    this.mode = 'view';
    this.connection.configuredProperties = this.configurationService.sanitize(this.formGroup.value);
    this.updated.emit(this.connection);
    this.resetView(true);
  }

  resetView(readOnly: boolean) {
    this.validating = false;
    this.validateError = undefined;
    this.validateSuccess = false;
    this.formModel = this.configurationService.getFormModel(this.connection, readOnly);
    this.formGroup = this.formService.createFormGroup(this.formModel);
  }

  showValidateButton(id: string) {
    return this.configurationService.shouldValidate(id);
  }

  doValidate(connector: Connector, formGroup: FormGroup) {
    this.validateSuccess = false;
    this.validateError = undefined;
    this.validating = true;
    const sanitized = this.configurationService.sanitize(formGroup.value);
    this.connectorStore.validate(connector.id, sanitized).subscribe(
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

}
