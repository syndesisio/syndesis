import { Injectable } from '@angular/core';
import { DynamicFormControlModel } from '@ng-dynamic-forms/core';

import { ConfigurationProperty } from '@syndesis/ui/model';

export interface ConfiguredConfigurationProperty extends ConfigurationProperty {
  value: any;
  rows?: number;
  cols?: number;
}

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
      | Map<string, ConfiguredConfigurationProperty>
      | Map<string, ConfigurationProperty>
      | Map<string, any>
      | any,
    values?: any,
    controls?: Array<string>
  ): DynamicFormControlModel[];
}
