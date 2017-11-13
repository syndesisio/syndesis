import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterStateSnapshot } from '@angular/router';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService
} from '@ng-dynamic-forms/core';
import { Subscription } from 'rxjs/Subscription';

import { CurrentConnectionService } from '../current-connection';
import { Connection } from '../../../model';
import { CanComponentDeactivate } from '../../../common/can-deactivate-guard.service';
import { ModalService } from '../../../common/modal/modal.service';
import { ConnectionConfigurationService } from '../../common/configuration/configuration.service';

@Component({
  selector: 'syndesis-connections-configure-fields',
  templateUrl: 'configure-fields.component.html'
})
export class ConnectionsConfigureFieldsComponent
  implements OnInit, OnDestroy, CanComponentDeactivate {
  formModel: DynamicFormControlModel[];
  formGroup: FormGroup;
  formChangesSubscription: Subscription;

  constructor(
    public current: CurrentConnectionService,
    public modalService: ModalService,
    private configurationService: ConnectionConfigurationService,
    private formService: DynamicFormService
  ) {}

  ngOnInit() {
    this.formModel = this.configurationService.getFormModel(
      this.connection,
      false
    );
    this.formGroup = this.formService.createFormGroup(this.formModel);
    this.formChangesSubscription = this.formGroup.valueChanges.subscribe(
      data => {
        Object.keys(data).forEach(key => {
          if (data[key] === null) {
            delete data[key];
          }
        });
        this.connection.configuredProperties = data;
      }
    );
  }

  ngOnDestroy() {
    this.formChangesSubscription.unsubscribe();
  }

  get connection(): Connection {
    return this.current.connection;
  }

  get hasCredentials() {
    return this.current.hasCredentials();
  }

  acquireCredentials() {
    this.current.acquireCredentials();
  }

  canDeactivate(nextState: RouterStateSnapshot) {
    if (this.clickedNextButFormInvalid(nextState)) {
      this.touchFormFields();
      return false;
    }
    return (
      nextState.url === '/connections/create/cancel' ||
      nextState.url === '/connections/create/connection-basics' ||
      nextState.url === '/connections/create/review' ||
      this.modalService.show().then(modal => modal.result)
    );
  }

  private clickedNextButFormInvalid(nextState: RouterStateSnapshot) {
    return (
      nextState.url === '/connections/create/review' && this.formGroup.invalid
    );
  }

  // This will trigger validation
  private touchFormFields() {
    Object.keys(this.formGroup.controls).forEach(key => {
      this.formGroup.get(key).markAsTouched();
    });
  }
}
