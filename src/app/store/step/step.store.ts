import { Injectable } from '@angular/core';
import { Connection, Action, Step, Steps, TypeFactory } from '../../model';

export interface StepKind extends Step {
  name: string;
  description: string;
  properties: any;
  visible?: (
    position: number,
    previous: Array<Step>,
    subsequent: Array<Step>,
  ) => boolean;
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
      visible: (
        position: number,
        previous: Array<Step>,
        subsequent: Array<Step>,
      ): boolean => {
        // previous and subsequent steps need to have input and output data shapes respectively
        const prev = previous.filter(s => {
          return (
            s.action &&
            s.action.outputDataShape &&
            s.action.outputDataShape.type
          );
        });
        if (!prev.length) {
          return false;
        }
        const subs = subsequent.filter(s => {
          return (
            s.action && s.action.inputDataShape && s.action.inputDataShape.type
          );
        });
        if (!subs.length) {
          return false;
        }
        return true;
      },
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
      name: 'Basic Filter',
      description:
        'Continue the integration only if criteria you specify in simple input fields are met. Suitable for' +
        ' most integrations.',
      stepKind: 'rule-filter',
      properties: undefined,
      configuredProperties: undefined,
    },
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Advanced Filter',
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
