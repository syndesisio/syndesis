import { DynamicInputModel, DynamicInputModelConfig, ClsConfig } from '@ng-dynamic-forms/core';

export class DurationInputModel extends DynamicInputModel {

  constructor(config: DynamicInputModelConfig, layout?: ClsConfig) {
    super(config, layout);
    this.inputType = 'duration';
  }

}
