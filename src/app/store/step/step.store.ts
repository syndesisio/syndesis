import { Injectable } from '@angular/core';
import { Step, Steps, TypeFactory } from '../../model';

export interface StepKind extends Step {
  name: string;
  description: string;
  properties: any;
}
export type StepKinds = Array<StepKind>;

@Injectable()
export class StepStore {
  steps: StepKind[] = [
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Data Mapper',
      description: 'Map fields from the input type to the output type',
      stepKind: 'mapper',
      properties: {},
      configuredProperties: undefined,
    },
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Log',
      stepKind: 'log',
      description: "Sends a message to the integration's log",
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
    },
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Filter',
      description: 'Filter incoming data based on a set of criteria',
      stepKind: 'filter',
      properties: {
        filter: {
          type: 'textarea',
          displayName: 'Only continue if',
          required: true,
          rows: 10,
        },
      },
      configuredProperties: undefined,
    },
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Store Data',
      stepKind: 'storeData',
      description:
        'Store data from an invocation to be used later in the integration',
      properties: {},
      configuredProperties: undefined,
    },
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Set Data',
      stepKind: 'setData',
      description: 'Enrich data used within an integration',
      properties: {},
      configuredProperties: undefined,
    },
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Call Route',
      stepKind: 'callRoute',
      description:
        'Call a child integration route from the main integration flow',
      properties: {},
      configuredProperties: undefined,
    },
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Conditional Processing',
      stepKind: 'conditionalProcessing',
      description: 'Add conditions and multiple paths for processing data',
      properties: {},
      configuredProperties: undefined,
    },
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Split',
      stepKind: 'split',
      description:
        'Split received data into data subsets that can be processed individually',
      properties: {},
      configuredProperties: undefined,
    },
  ];

  getStepConfig(kind: string) {
    return this.steps.find(step => step.stepKind === kind);
  }

  getSteps() {
    return this.steps;
  }
}
