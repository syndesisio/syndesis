import {
  Component,
  Input,
  Output,
  OnInit,
  OnDestroy,
  EventEmitter
} from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';

import { CustomValidators } from '@syndesis/ui/platform';
import { CustomConnectorRequest } from '@syndesis/ui/customizations/api-connector';

@Component({
  selector: 'syndesis-api-connector-auth',
  templateUrl: './api-connector-auth.component.html'
})
export class ApiConnectorAuthComponent implements OnInit, OnDestroy {
  authSetupForm: FormGroup;
  authSetupFormValueSubscription: Subscription;

  @Input() customConnectorRequest: CustomConnectorRequest;
  @Output() authSetup = new EventEmitter();
  @Output() onBack = new EventEmitter<boolean>();
  @Output() onDone = new EventEmitter<void>();

  constructor(private formBuilder: FormBuilder) {}

  ngOnInit() {
    const {
      authenticationType,
      authorizationEndpoint,
      tokenEndpoint
    } = this.customConnectorRequest.configuredProperties;

    const properties = this.customConnectorRequest.properties;
    const defaultAuthenticationType =
      authenticationType ? authenticationType
                         : (properties.authenticationType && properties.authenticationType.defaultValue)
                           || (properties.authenticationType.enum.length > 0 && properties.authenticationType.enum[0].value);
    this.authSetupForm = this.formBuilder.group({
      authenticationType: [
        authenticationType ? authenticationType : defaultAuthenticationType
      ],
      authorizationEndpoint: [
        authorizationEndpoint ? authorizationEndpoint
                              : properties.authorizationEndpoint ? properties.authorizationEndpoint.defaultValue
                                                                 : ''
      ],
      tokenEndpoint: [
        tokenEndpoint ? tokenEndpoint
                      : properties.tokenEndpoint ? properties.tokenEndpoint.defaultValue
                                                 : ''
      ]
    });

    this.authSetupFormValueSubscription = this.authSetupForm
      .get('authenticationType')
      .valueChanges.subscribe(value => this.setOAuthFormValidation(value));

    this.setOAuthFormValidation( defaultAuthenticationType );
  }

  get authUrl() {
    return this.authSetupForm.get( 'authorizationEndpoint' );
  }

  get tokenUrl() {
    return this.authSetupForm.get( 'tokenEndpoint' );
  }

  onSubmit({ value, valid }): void {
    if (valid) {
      this.authSetup.emit(value);
      this.onDone.emit();
    }
  }

  ngOnDestroy() {
    this.authSetupFormValueSubscription.unsubscribe();
  }

  private setOAuthFormValidation(authenticationType: string) {
    const validatorFn =
      authenticationType == 'oauth2'
        ? [Validators.required, CustomValidators.validUrl]
        : [];
    const {
      authorizationEndpoint,
      tokenEndpoint
    } = this.authSetupForm.controls;

    authorizationEndpoint.setValidators(validatorFn);
    authorizationEndpoint.updateValueAndValidity();
    tokenEndpoint.setValidators(validatorFn);
    tokenEndpoint.updateValueAndValidity();
  }
}
