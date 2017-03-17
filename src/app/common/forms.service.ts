import { Injectable } from '@angular/core';
import { ComponentProperty } from '../model';
import { DynamicFormControlModel, DynamicCheckboxModel, DynamicInputModel } from '@ng2-dynamic-forms/core';

export interface ConfiguredComponentProperty extends ComponentProperty {
  value: any;
}

@Injectable()
export class FormFactoryService {

  createFormModel(properties: Map<string, ConfiguredComponentProperty> |
                              Map<string, ComponentProperty> |
                              Map<string, any> |
                              {}): DynamicFormControlModel[] {
    const answer = <DynamicFormControlModel[]>[];
    for (const key in properties) {
      if (!properties.hasOwnProperty(key)) {
        continue;
      }
      const value = <ConfiguredComponentProperty> properties[key];
      let formField: any;
      let type = (value.type || '').toLowerCase();
      // first normalize the type
      switch (type.toLowerCase()) {
        case 'boolean':
          type = 'checkbox';
          break;
        case 'number':
        case 'integer':
        case 'long':
          type = 'number';
          break;
        case 'password':
          type = 'password';
          break;
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
      } else {
        if (value.secret) {
          type = 'password';
        }
        formField = new DynamicInputModel({
          id: key,
          label: type === 'hidden' ? null : value.displayName || key,
          placeholder: type === 'hidden' ? null : value.description,
          inputType: type,
          value: value.value || value.defaultValue,
          required: value.required,
        }, {
          element: {
            label: 'control-label',
          },
          grid: {
            control: 'col-sm-9',
            label: 'col-sm-3',
          },
        });
      }

      if (formField) {
        answer.push(formField);
      }
    }
    return answer;
  }

}
