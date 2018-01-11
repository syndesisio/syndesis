import {
  Component,
  OnInit,
  EventEmitter,
  Input,
  Output,
} from '@angular/core';
import { OAuthApp, OAuthApps } from '../../model';
import { FormFactoryService } from '../../common/forms.service';
import { OAuthAppStore } from '../../store/oauthApp/oauth-app.store';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService,
  DynamicInputModel
} from '@ng-dynamic-forms/core';

const uniqueNum = () => {
  console.log('uniqueNum called');
  return Math.random().toString(36).substr(2, 16);
}

const uniqueIdProp = () => 'clientId-' + uniqueNum();
const uniqueSecretProp = () => 'clientSecret-' + uniqueNum();
const idName = uniqueIdProp();
const secretName = uniqueSecretProp();
const OAUTH_APP_FORM_CONFIG = {};

OAUTH_APP_FORM_CONFIG[idName] = {
  displayName: 'Client ID',
  type: 'string',
  description: 'The OAuth client ID setting for the target application'
};

OAUTH_APP_FORM_CONFIG[secretName] = {
  displayName: 'Client Secret',
  type: 'password',
  description: 'The OAuth client secret value for the target application'
};

@Component({
  selector: 'syndesis-oauth-app-form',
  templateUrl: 'oauth-app-form.component.html'
})
export class OAuthAppFormComponent implements OnInit {
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
  ) {}

  save() {
    const app = { ...this.item.client, ...this.formGroup.value };
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

  ngOnInit() {
    const formConfig = JSON.parse(JSON.stringify(OAUTH_APP_FORM_CONFIG));
    this.formModel = this.formFactory.createFormModel(
      formConfig,
      this.item.client
    );
    this.formGroup = this.formService.createFormGroup(this.formModel);
  }
}
