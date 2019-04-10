import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterStateSnapshot } from '@angular/router';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService
} from '@ng-dynamic-forms/core';
import { Subscription } from 'rxjs';

import { Connection, CanComponentDeactivate } from '@syndesis/ui/platform';
import { CurrentConnectionService } from '@syndesis/ui/connections/create-page/current-connection';
import { ModalService } from '@syndesis/ui/common/modal/modal.service';
import { ConnectionConfigurationService } from '@syndesis/ui/connections/common/configuration/configuration.service';

@Component({
  selector: 'syndesis-connections-configure-fields',
  templateUrl: 'configure-fields.component.html'
})
export class ConnectionsConfigureFieldsComponent
  implements OnInit, OnDestroy, CanComponentDeactivate {
  formModel: DynamicFormControlModel[];
  formGroup: FormGroup;
  formChangesSubscription: Subscription;
  acquiringCredentials = false;

  constructor(
    public current: CurrentConnectionService,
    public modalService: ModalService,
    private configurationService: ConnectionConfigurationService,
    private formService: DynamicFormService
  ) {}

  ngOnInit() {
    this.formModel = this.configurationService.getFormModel(this.connection);
    this.formGroup = this.formService.createFormGroup(this.formModel);
    const updateConnectionConfig =
      data => this.connection.configuredProperties = this.configurationService.sanitize(data);
    // Set any initial default config
    updateConnectionConfig(this.formGroup.value);
    // Update the config when there's form changes
    this.formChangesSubscription =
      this.formGroup.valueChanges.subscribe(updateConnectionConfig);
    this.current.formGroup = this.hasCredentials ? null : this.formGroup;
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
    this.acquiringCredentials = true;
    let sub: Subscription;
    const callback = () => {
      this.acquiringCredentials = false;
      if (sub) {
        sub.unsubscribe();
      }
    };
    // error info is directly exposed from the current connection service, just toggle the spinner from here
    sub = this.current.acquireCredentials().subscribe(callback, callback);
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
