import { Injectable } from '@angular/core';
import { DynamicFormControlModel } from '@ng-dynamic-forms/core';
import { ConfigurationProperty, ConfiguredConfigurationProperty, StringMap } from '@syndesis/ui/platform';

@Injectable()
export abstract class FormFactoryService {
  /**
   * Generates a dynamic form control model, suitable for NG Dynamic Forms implementations
   * @param properties Overloaded configuration properties - To be further documented
   * @param values Default values for forms - To be further documented
   * @param controls Controls - To be further documented
   */
  abstract createFormModel(
    properties?:
      | StringMap<ConfiguredConfigurationProperty>
      | StringMap<ConfigurationProperty>
      | StringMap<any>
      | any,
    values?: any,
    controls?: Array<string>
  ): DynamicFormControlModel[];

  supressNullValues(data: object): object {
    Object.keys(data).forEach(key => {
      if (data[key] === null) {
        delete data[key];
      }
    });
    return data;
  }
}
