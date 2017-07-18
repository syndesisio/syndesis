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
  ): DynamicFormControlModel[] {
    const answer = <DynamicFormControlModel[]>[];
    for (const key in properties) {
      if (!properties.hasOwnProperty(key)) {
        continue;
      }
      const value = <ConfiguredConfigurationProperty>properties[key];
      let formField: any;
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
        formField = new DynamicCheckboxModel({
          id: key,
          label: value.displayName || key,
          hint: value.description,
          value: value.value || value.defaultValue,
        });
      } else if (type === 'textarea') {
        formField = new DynamicTextAreaModel(
          {
            id: key,
            label: value.displayName || key,
            hint: value.description,
            value: value.value || value.defaultValue,
            required: value.required,
            rows: value.rows,
            cols: value.cols,
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
            hint: type === 'hidden' ? null : value.description,
            inputType: type,
            value: value.value || value.defaultValue,
            required: value.required,
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
