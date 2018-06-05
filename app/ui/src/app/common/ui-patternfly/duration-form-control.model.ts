import {
  DynamicInputModel,
  DynamicInputModelConfig,
  DynamicFormControlLayout
} from '@ng-dynamic-forms/core';

export class DurationInputModel extends DynamicInputModel {
  constructor(
    config: DynamicInputModelConfig,
    layout?: DynamicFormControlLayout
  ) {
    super(config, layout);
    this.inputType = 'duration';
  }
}
