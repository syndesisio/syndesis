import { Injectable } from '@angular/core';
import { FormFactoryService, ConfigurationProperty, ConfiguredConfigurationProperty } from '@syndesis/ui/platform';
import { DurationInputModel } from '@syndesis/ui/common/ui-patternfly/duration-form-control.model';
import {
  DynamicFormControlModel,
  DynamicCheckboxModel,
  DynamicInputModel,
  DynamicTextAreaModel,
  DynamicSelectModel
} from '@ng-dynamic-forms/core';

@Injectable()
export class FormFactoryProviderService extends FormFactoryService {

  constructor() {
    super();
  }

  createFormModel(
    properties:
      | Map<string, ConfiguredConfigurationProperty>
      | Map<string, ConfigurationProperty>
      | Map<string, any>
      | {},
    values: any = {},
    controls: Array<string> = ['*']
  ): DynamicFormControlModel[] {
    const answer = [] as Array<DynamicFormControlModel>;
    for (const key in properties) {
      if (!properties.hasOwnProperty(key)) {
        continue;
      }
      const field = properties[key] as ConfiguredConfigurationProperty;
      const value = values[key];
      let formField: any;
      const validators = field.required ? { required: null } : {};
      const errorMessages = field.required
        ? { required: '{{label}} is required' }
        : {};
      let type = (field.type || '').toLowerCase();
      // first normalize the type
      switch (type) {
        case 'boolean':
          type = 'checkbox';
          break;
        case 'number':
        case 'integer':
        case 'long':
          type = 'number';
          break;
        case 'password':
        case 'textarea':
        case 'hidden':
          break;
        default:
          type = field.enum && field.enum.length ? 'select' : 'text';
          break;
      }
      // then use the appropriate ng2 dynamic forms constructor
      switch (type) {
        case 'duration':
          formField = this.createDuration(key, field, value);
          break;
        case 'checkbox':
          formField = this.createCheckbox(key, field, value);
          break;
        case 'textarea':
          formField = this.createTextArea(key, field, value, validators, errorMessages);
          break;
        case 'select':
          formField = this.createSelect(key, field, value, validators, errorMessages);
          break;
        default:
          formField = this.createInput(key, field, value, type,  validators, errorMessages);
      }

      if (formField) {
        answer.push(formField);
      }
    }
    // guard against missing wildcard
    if (controls.find(val => val === '*') === undefined) {
      controls.push('*');
    }
    // sort the form based on the controls array
    const wildcardIndex = controls.findIndex(val => val === '*');
    answer.sort((a, b) => {
      let aIndex = controls.findIndex(val => val === a.id);
      if (aIndex === -1) {
        aIndex = wildcardIndex;
      }
      let bIndex = controls.findIndex(val => val === b.id);
      if (bIndex === -1) {
        bIndex = wildcardIndex;
      }
      return aIndex - bIndex;
    });
    return answer;
  }

  private createDuration(key: string,
                         field: ConfiguredConfigurationProperty,
                         value: any): any {
    return new DurationInputModel({
      id: key,
      label: field.displayName || key,
      inputType: 'duration',
      value: value || field.value || field.defaultValue,
      hint: field.description,
      required: field.required,
    }, {
        element: {
          label: 'control-label'
        },
        grid: {
          control: 'col-sm-9',
          label: 'col-sm-3'
        }
      });
  }

  private createInput(
                      key: string,
                      field: ConfiguredConfigurationProperty,
                      value: any,
                      type: string,
                      validators: {},
                      errorMessages: {}) {
    if (field.secret) {
      type = 'password';
    }
    return new DynamicInputModel({
      id: key,
      label: type === 'hidden' ? null : field.displayName || key,
      inputType: type,
      value: value || field.value || field.defaultValue,
      hint: field.description,
      list: field.enum
        ? (<Array<any>>field.enum).map(val => {
          if (typeof val === 'string') {
            return val;
          }
          return val['value'];
        })
        : undefined,
      required: type === 'hidden' ? undefined : field.required,
      autoComplete: field.secret ? 'off' : undefined,
      validators: type === 'hidden' ? undefined : validators,
      errorMessages: errorMessages
    }, {
        element: {
          label: 'control-label'
        },
        grid: {
          control: 'col-sm-9',
          label: 'col-sm-3'
        }
      });
  }

  private createSelect(key: string,
                       field: ConfiguredConfigurationProperty,
                       value: any,
                       validators: {},
                       errorMessages: {}) {
    return new DynamicSelectModel({
      id: key,
      multiple: false,
      label: field.displayName || key,
      value: value || field.defaultValue || field.enum[0].value,
      hint: field.description,
      required: field.required,
      validators: validators,
      errorMessages: errorMessages,
      options: field.enum
    }, {
        element: {
          label: 'control-label'
        },
        grid: {
          control: 'col-sm-9',
          label: 'col-sm-3'
        }
      });
  }

  private createTextArea(key: string,
                         field: ConfiguredConfigurationProperty,
                         value: any,
                         validators: {},
                         errorMessages: {}) {
    return new DynamicTextAreaModel({
      id: key,
      label: field.displayName || key,
      value: value || field.value || field.defaultValue,
      hint: field.description,
      required: field.required,
      rows: field.rows,
      cols: field.cols,
      validators: validators,
      errorMessages: errorMessages
    }, {
        element: {
          label: 'control-label'
        },
        grid: {
          control: 'col-sm-9',
          label: 'col-sm-3'
        }
      });
  }

  private createCheckbox(key: string,
                         field: ConfiguredConfigurationProperty,
                         value: any
  ) {
    return new DynamicCheckboxModel({
      id: key,
      label: field.displayName || key,
      hint: field.description,
      value: value || field.value || field.defaultValue
    }, {
        element: {
          control: 'element-control checkbox'
        },
        grid: {
          control: 'col-sm-offset-3 col-sm-9',
          label: ''
        }
      });
  }
}
