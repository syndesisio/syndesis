import { Injectable } from '@angular/core';
import { Step, Steps, TypeFactory } from '../../model';

@Injectable()
export class StepStore {

  steps: any[] = [
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      stepKind: 'log',
      configuredProperties: undefined,
      properties: {
        message: {
          type: 'string',
          displayName: 'Log Message',
          required: true,
        },
        loggingLevel: {
          type: 'hidden',
          displayName: 'Level',
          defaultValue: 'INFO',
          required: true,
        },
      },
    }, {
      id: undefined,
      connection: undefined,
      action: undefined,
      stepKind: 'datamapper',
      properties: {},
      configuredProperties: undefined,
    },
  ];

  getStepConfig(kind: string) {
    return this.steps.find((step) => step.stepKind === kind);
  }

  getSteps() {
    return this.steps;
  }


}
