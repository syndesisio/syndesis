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
      switch (value.type.toLowerCase()) {
        case 'string':
        case 'text':
        case 'number':
        case 'boolean': // TODO
        case 'java.lang.string':
          formField = new DynamicInputModel({
            id: value.name,
            label: value.title,
            hint: value.description,
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
