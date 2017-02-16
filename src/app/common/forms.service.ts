import { Injectable } from '@angular/core';
import { DynamicFormControlModel, DynamicInputModel } from '@ng2-dynamic-forms/core';

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
      switch (value.type.toLowerCase()) {
        case 'string':
        case 'text':
        case 'number':
        case 'boolean':
        case 'password':
        case 'java.lang.string':
          if (type === 'java.lang.string') {
            type = 'text';
          }
          formField = new DynamicInputModel({
            id: value.name || key,
            label: value.title || value.displayName || value.name || key,
            hint: value.description,
            inputType: type,
          });
          break;
        default:
          break;
      }
      if (formField) {
        answer.push(formField);
      }
    }
    return answer;
  }

}
