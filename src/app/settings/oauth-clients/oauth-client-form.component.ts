import { Component, OnInit, EventEmitter, Input, Output } from '@angular/core';
import { FormFactoryService } from '../../common/forms.service';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService,
  DynamicInputModel,
} from '@ng2-dynamic-forms/core';

// Default form config object for OAuth client settings
const OAUTH_CLIENT_FORM_CONFIG = {
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
export function getOAuthClientForm(settings: any) {
  const answer = JSON.parse(JSON.stringify(OAUTH_CLIENT_FORM_CONFIG));
  for (const key of Object.keys(answer)) {
    answer[key].value = settings[key];
  }
  return answer;
}

@Component({
  selector: 'syndesis-oauth-client-form',
  templateUrl: 'oauth-client-form.component.html',
})
export class OAuthClientFormComponent implements OnInit {
  @Input() item: any = {};
  formGroup: FormGroup;
  formModel: DynamicFormControlModel[];

  constructor(
    private formFactory: FormFactoryService,
    private formService: DynamicFormService,
  ) {}

  ngOnInit() {
    const formConfig = getOAuthClientForm(this.item.client);
    this.formModel = this.formFactory.createFormModel(formConfig);
    this.formGroup = this.formService.createFormGroup(this.formModel);
  }
}
