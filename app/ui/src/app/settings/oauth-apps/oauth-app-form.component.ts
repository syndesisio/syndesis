import { Component, OnInit, EventEmitter, Input, Output } from '@angular/core';
import { Router } from '@angular/router';
import { OAuthApp, OAuthApps } from '@syndesis/ui/settings';
import { FormFactoryService } from '@syndesis/ui/platform';
import { OAuthAppStore } from '../../store/oauthApp/oauth-app.store';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService,
  DynamicInputModel
} from '@ng-dynamic-forms/core';

// Default form config object for OAuth client settings
const OAUTH_APP_FORM_CONFIG = {
  clientId: {
    displayName: 'Client ID',
    type: 'string',
    labelHint: 'The OAuth client ID setting for the target application'
  },
  clientSecret: {
    displayName: 'Client Secret',
    type: 'password',
    labelHint: 'The OAuth client secret value for the target application'
  },
  authorizationUrl: {
    displayName: 'Authorization URL',
    type: 'string',
    labelHint: 'URL to the authorization endpoint of the target application'
  },
  tokenUrl: {
    displayName: 'Access Token URL',
    type: 'string',
    labelHint: 'URL to the access token endpoint of the target application'
  },
  scopes: {
    displayName: 'OAuth scopes',
    type: 'string',
    labelHint:
      'Comma separated list of OAuth scopes used when requesting authorization from the target application'
  }
};

@Component({
  selector: 'syndesis-oauth-app-form',
  templateUrl: 'oauth-app-form.component.html'
})
export class OAuthAppFormComponent implements OnInit {
  formConfig: any;
  @Input() item: any = {};

  loading = false;
  error: any = null;
  message: any = null;
  formGroup: FormGroup;
  formModel: DynamicFormControlModel[];

  constructor(
    private formFactory: FormFactoryService,
    private formService: DynamicFormService,
    private store: OAuthAppStore,
    private router: Router
  ) {}

  save() {
    const app = {
      ...this.item.client,
      ...this.formFactory.sanitizeValues(this.formGroup.value, this.formConfig)
    };
    this.formGroup.disable();
    this.loading = true;
    this.error = null;
    this.message = null;
    const sub = this.store.update(app).subscribe(
      () => {
        this.loading = false;
        this.message = 'success';
        sub.unsubscribe();
        this.item.client = app;
      },
      (error: any) => {
        this.loading = false;
        this.formGroup.enable();
        this.message = null;
        this.error = error;
        sub.unsubscribe();
      }
    );
  }

  handleLinks(event: any): void {
    event.stopPropagation();
    event.preventDefault();

    if (
      event.target &&
      event.target.tagName &&
      event.target.tagName.toLowerCase() === 'a'
    ) {
      this.router.navigateByUrl(event.target.getAttribute('href'));
    }
  }

  ngOnInit() {
    const formConfig = (this.formConfig = JSON.parse(
      JSON.stringify(OAUTH_APP_FORM_CONFIG)
    ));
    this.formModel = this.formFactory.createFormModel(
      formConfig,
      this.item.client,
      ['*']
    );
    this.formGroup = this.formService.createFormGroup(this.formModel);
  }
}
