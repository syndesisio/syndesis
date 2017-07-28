import {
  Component,
  OnInit,
  EventEmitter,
  Input,
  Output,
  ChangeDetectorRef,
} from '@angular/core';
import { OAuthApp, OAuthApps } from '../../model';
import { FormFactoryService } from '../../common/forms.service';
import { OAuthAppStore } from '../../store/oauthApp/oauth-app.store';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService,
  DynamicInputModel,
} from '@ng2-dynamic-forms/core';

// Default form config object for OAuth client settings
const OAUTH_APP_FORM_CONFIG = {
  clientId: {
    displayName: 'Client ID',
    type: 'string',
    description: 'The OAuth client ID setting for the target application',
  },
  clientSecret: {
    displayName: 'Client Secret',
    type: 'password',
    description: 'The OAuth client secret value for the target application',
  },
};

/**
 * Returns a form configuration object for the supplied OAuth client settings object
 *
 * @export
 * @param {*} settings
 * @returns
 */
export function getOAuthAppForm(settings: any) {
  const answer = JSON.parse(JSON.stringify(OAUTH_APP_FORM_CONFIG));
  for (const key of Object.keys(answer)) {
    answer[key].value = settings[key];
  }
  return answer;
}

@Component({
  selector: 'syndesis-oauth-app-form',
  templateUrl: 'oauth-app-form.component.html',
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
    public detector: ChangeDetectorRef,
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
        this.detector.detectChanges();
      },
      (error: any) => {
        this.loading = false;
        this.formGroup.enable();
        this.message = null;
        this.error = error;
        sub.unsubscribe();
        this.detector.detectChanges();
      },
    );
  }

  ngOnInit() {
    const formConfig = getOAuthAppForm(this.item.client);
    this.formModel = this.formFactory.createFormModel(formConfig);
    this.formGroup = this.formService.createFormGroup(this.formModel);
  }
}
