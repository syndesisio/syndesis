import { Injectable } from '@angular/core';
import { DynamicFormControlModel, DynamicCheckboxModel, DynamicInputModel } from '@ng2-dynamic-forms/core';

@Injectable()
export class FormFactoryService {

  createFormModel(properties: Map<string, any>): DynamicFormControlModel[] {
    const answer = <DynamicFormControlModel[]>[];
    for (const key in properties) {
      if (!properties.hasOwnProperty(key)) {
        continue;
      }
      const value: any = properties[key];
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
        default:
          type = 'text';
          break;
      }
      // then use the appropriate ng2 dynamic forms constructor
      if (type === 'checkbox') {
        formField = new DynamicCheckboxModel({
          id: value.name || key,
          label: value.title || value.displayName || value.name || key,
          hint: value.description,
          value: value.value || value.defaultValue,
        });
      } else {
        formField = new DynamicInputModel({
          id: value.name || key,
          label: value.title || value.displayName || value.name || key,
          hint: value.description,
          inputType: type,
          value: value.value || value.defaultValue,
        });
      }

      if (formField) {
        answer.push(formField);
      }
    }
    return answer;
  }

}
