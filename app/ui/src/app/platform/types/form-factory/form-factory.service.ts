import { Injectable } from '@angular/core';
import { DynamicFormControlModel } from '@ng-dynamic-forms/core';

import {
  ConfigurationProperty,
  ConfiguredConfigurationProperty,
  StringMap
} from '@syndesis/ui/platform';

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

  sanitizeValues(
    data: any,
    properties:
      | StringMap<ConfiguredConfigurationProperty>
      | StringMap<ConfigurationProperty>
      | StringMap<any>
      | any
  ): any {
    data = this.supressNullValues(data);
    data = this.trimStringValues(data, properties);
    return data;
  }

  supressNullValues(data: any): any {
    if (!data) {
      return data;
    }
    Object.keys(data).forEach(key => {
      if (data[key] === null) {
        delete data[key];
      }
    });
    return data;
  }

  trimStringValues(
    data: any,
    properties:
      | StringMap<ConfiguredConfigurationProperty>
      | StringMap<ConfigurationProperty>
      | StringMap<any>
      | any
  ): any {
    if (!properties || !data) {
      return data;
    }
    Object.keys(data).forEach(key => {
      const prop = properties[key];
      if (prop.secret && data[key] === '') {
        delete data[key];
      }
      if ((prop && prop.trim === false) || prop.secret) {
        return;
      }
      const value = data[key];
      if (typeof value === 'string') {
        data[key] = value.trim();
      }
    });
    return data;
  }
}
