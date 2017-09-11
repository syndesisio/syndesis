import { Injectable } from '@angular/core';
import { ConfigurationProperty } from '../model';
import {
  DynamicFormControlModel,
  DynamicCheckboxModel,
  DynamicInputModel,
  DynamicTextAreaModel,
} from '@ng2-dynamic-forms/core';

export interface ConfiguredConfigurationProperty extends ConfigurationProperty {
  value: any;
  rows?: number;
  cols?: number;
}

@Injectable()
export class FormFactoryService {
  createFormModel(
    properties:
      | Map<string, ConfiguredConfigurationProperty>
      | Map<string, ConfigurationProperty>
      | Map<string, any>
      | {},
    values: any = {},
  ): DynamicFormControlModel[] {
    const answer = <DynamicFormControlModel[]>[];
    for (const key in properties) {
      if (!properties.hasOwnProperty(key)) {
        continue;
      }
      const value = <ConfiguredConfigurationProperty>properties[key];
      let formField: any;
      const validators = value.required ? {'required': null} : {};
      const errorMessages = value.required ? {'required': '{{label}} is required'} : {};
      let type = (value.type || '').toLowerCase();
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
          type = 'text';
          break;
      }
      // then use the appropriate ng2 dynamic forms constructor
      if (type === 'checkbox') {
        formField = new DynamicCheckboxModel(
          {
            id: key,
            label: value.displayName || key,
            hint: value.description,
            value: value.value || values[key] || value.defaultValue,
          },
          {
            element: {
              control: 'element-control checkbox',
            },
            grid: {
              control: 'col-sm-offset-3 col-sm-9',
              label: '',
            },
          },
        );
      } else if (type === 'textarea') {
        formField = new DynamicTextAreaModel(
          {
            id: key,
            label: value.displayName || key,
            value: value.value || values[key] || value.defaultValue,
            hint: value.description,
            required: value.required,
            rows: value.rows,
            cols: value.cols,
            validators: validators,
            errorMessages: errorMessages,
          },
          {
            element: {
              label: 'control-label',
            },
            grid: {
              control: 'col-sm-9',
              label: 'col-sm-3',
            },
          },
        );
      } else {
        if (value.secret) {
          type = 'password';
        }
        formField = new DynamicInputModel(
          {
            id: key,
            label: type === 'hidden' ? null : value.displayName || key,
            inputType: type,
            value: value.value || values[key] || value.defaultValue,
            hint: value.description,
            required: value.required,
            validators: validators,
            errorMessages: errorMessages,
          },
          {
            element: {
              label: 'control-label',
            },
            grid: {
              control: 'col-sm-9',
              label: 'col-sm-3',
            },
          },
        );
      }

      if (formField) {
        answer.push(formField);
      }
    }
    return answer;
  }
}
