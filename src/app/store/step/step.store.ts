import { Injectable } from '@angular/core';
import { Step, Steps, TypeFactory } from '../../model';

@Injectable()
export class StepStore {

  steps: Steps = [
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      stepKind: 'log',
      configuredProperties: JSON.stringify({
        message: {
          type: 'string',
          displayName: 'Log Message',
          required: true,
        },
        loggingLevel: {
          type: 'string',
          displayName: 'Level',
          required: true,
        },
      }),
    }, {
      id: undefined,
      connection: undefined,
      action: undefined,
      stepKind: 'datamapper',
      configuredProperties: JSON.stringify({

      }),
    },
  ];

  getStepConfig(kind: string) {
    return this.steps.find((step) => step.stepKind === kind);
  }

  getSteps() {
    return this.steps;
  }


}
