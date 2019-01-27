import { Injectable } from '@angular/core';
import {
  Extensions,
  Step,
  DataShapeKinds
} from '@syndesis/ui/platform';

export interface StepKind extends Step {
  name: string;
  description: string;
  properties: any;
  visible?: (
    position: number,
    previous: Array<Step>,
    subsequent: Array<Step>
  ) => boolean;
}
export type StepKinds = Array<StepKind>;

export const EXTENSION = 'extension';
export const ENDPOINT = 'endpoint';
export const CONNECTION = ENDPOINT;
export const DATA_MAPPER = 'mapper';
export const BASIC_FILTER = 'ruleFilter';
export const ADVANCED_FILTER = 'expressionFilter';
export const STORE_DATA = 'storeData';
export const SET_DATA = 'setData';
export const CALL_ROUTE = 'callRoute';
export const CONDITIONAL_PROCESSING = 'conditionalProcessing';
export const SPLIT = 'split';
export const FOREACH = 'foreach';
export const END_FOREACH = 'endForeach';
export const LOG = 'log';
export const TEMPLATE = 'template';

@Injectable()
export class StepStore {
  steps: StepKind[] = [
    StepStore.requiresInputOutputDataShapes({
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Data Mapper',
      description: 'Map fields from the input type to the output type.',
      stepKind: DATA_MAPPER,
      properties: {},
      configuredProperties: undefined
    }, true, false),
    StepStore.requiresOutputDataShape({
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Basic Filter',
      description:
        'Continue the integration only if criteria you specify in simple input fields are met. Suitable for' +
        ' most integrations.',
      stepKind: BASIC_FILTER,
      properties: undefined,
      configuredProperties: undefined
    }),
    StepStore.requiresOutputDataShape({
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Template',
      stepKind: TEMPLATE,
      description: 'Upload or create a Freemarker, Mustache or Velocity template to define consistent output data.',
      configuredProperties: undefined,
      properties: undefined
    }),
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Advanced Filter',
      description:
        'Continue the integration only if criteria you define in scripting language expressions are met.',
      stepKind: ADVANCED_FILTER,
      properties: {
        filter: {
          type: 'textarea',
          displayName: 'Only continue if',
          placeholder: `Examples of Simple Language filter expressions:

$\{in.header.type\} == 'widget' // Evaluates true when type = widget
$\{in.body.title\} // Evaluates true when body contains title.
`,
          required: true,
          rows: 10
        }
      },
      configuredProperties: undefined
    },
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Log',
      stepKind: LOG,
      description: "Send a message to the integration's log.",
      configuredProperties: undefined,
      properties: {
        contextLoggingEnabled: {
          type: 'boolean',
          displayName: 'Message Context',
          required: false
        },
        bodyLoggingEnabled: {
          type: 'boolean',
          displayName: 'Message Body',
          required: false
        },
        customText: {
          type: 'string',
          displayName: 'Custom Text',
          required: false
        }
        /*
        loggingLevel: {
          type: 'select',
          displayName: 'Level',
          value: 'INFO',
          required: true,
          enum: [
            { value: 'INFO', label: 'INFO' },
            { value: 'WARN', label: 'WARN' },
            { value: 'ERROR', label: 'ERROR'},
            {value: 'DEBUG', label: 'DEBUG'},
            {value: 'TRACE', label: 'TRACE'}],
        },
        */
      }
    },
    StepStore.requiresOutputDataShape({
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Foreach',
      description: 'Process each item in a set of data individually',
      stepKind: FOREACH,
      properties: {},
      configuredProperties: undefined
    }),
    StepStore.requiresForeach({
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'EndForeach',
      description: 'End processing items in a foreach',
      stepKind: END_FOREACH,
      properties: {},
      configuredProperties: undefined
    })
    /*
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Store Data',
      stepKind: STORE_DATA,
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
      stepKind: SET_DATA,
      description: 'Enrich data used within an integration',
      properties: {},
      configuredProperties: undefined,
    },
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Call Route',
      stepKind: CALL_ROUTE,
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
      stepKind: CONDITIONAL_PROCESSING,
      description: 'Add conditions and multiple paths for processing data',
      properties: {},
      configuredProperties: undefined,
    }
    */
  ];

  getStepName(step: any): string {
    if (step) {
      return step.name;
    }
    return 'step';
  }

  getStepDescription(step: any): string {
    if (step) {
      return step.description;
    }
    return '';
  }

  getProperties(step: Step) {
    const action = step.action;
    if (!action) {
      // it's a legacy step
      const _step = this.getStepConfig(step.stepKind);
      return _step.properties;
    }
    // flatten arrays of properties into one object until step configuration supports pages
    if (Array.isArray(action.descriptor.propertyDefinitionSteps)) {
      return action.descriptor.propertyDefinitionSteps.reduce(
        (acc, current) => {
          return { ...acc, ...current.properties };
        },
        {}
      );
    } else {
      return {};
    }
  }

  getSteps(extensions: Extensions = []) {
    const allSteps = [];
    for (const extension of extensions) {
      if (extension.actions) {
        for (const action of extension.actions) {
          if (action.actionType == 'step') {
            let properties;
            if (Array.isArray(action.descriptor.propertyDefinitionSteps)) {
              properties = action.descriptor.propertyDefinitionSteps.reduce(
                (acc, current) => {
                  return { ...acc, ...current.properties };
                },
                {}
              );
            } else {
              properties = {};
            }
            allSteps.push({
              name: action.name,
              description: action.description,
              stepKind: 'extension',
              properties: properties,
              extension: extension,
              action: action,
              configuredProperties: undefined
            });
          }
        }
      }
    }
    return this.steps
      .concat(allSteps)
      .sort((a, b) => a.name.localeCompare(b.name));
  }

  // Check if we need a custom form handling which stores the parsed
  // properties in customProperties
  isCustomStep(step: Step): boolean {
    return step.stepKind === BASIC_FILTER ||
            step.stepKind === DATA_MAPPER ||
            step.stepKind === TEMPLATE;
  }

  getDefaultStepDefinition(stepKind: String): Step {
    if (!stepKind) {
      return undefined;
    }
    // pull out attributes that aren't in the backend model
    const { description, visible, ...step } = this.getStepConfig(stepKind);
    return step;
}

  static stepsHaveOutputDataShape(steps: Array<Step>): boolean {
    return (
      steps.filter(
        s =>
          s.action &&
          s.action.descriptor.outputDataShape &&
          s.action.descriptor.outputDataShape.kind !== DataShapeKinds.NONE &&
          s.action.descriptor.outputDataShape.kind !== DataShapeKinds.ANY
      ).length > 0
    );
  }

  static stepsHaveInputDataShape(steps: Array<Step>): boolean {
    return (
      steps.filter(
        s =>
          s.action &&
          s.action.descriptor.inputDataShape &&
          s.action.descriptor.inputDataShape.kind !== DataShapeKinds.NONE &&
          s.action.descriptor.inputDataShape.kind !== DataShapeKinds.ANY
      ).length > 0
    );
  }

  static requiresInputOutputDataShapes(obj: StepKind, anyPrevious = true, anySubsequent = true): StepKind {
    obj.visible = (
      position: number,
      previousSteps: Array<Step>,
      subsequentSteps: Array<Step>
    ) => {
      if (!anyPrevious) {
        // only test the first previous step that has some kind of data shape
        previousSteps = [
          [].concat(previousSteps)
            .reverse()
            .find(s => StepStore.dataShapeExists(s))
        ];
      }
      if (!anySubsequent) {
        // only test the next subsequent step that has a data shape
        subsequentSteps = [ subsequentSteps
          .find(s => StepStore.dataShapeExists(s, true))
        ];

      }
      return StepStore.stepsHaveOutputDataShape(previousSteps)
         && StepStore.stepsHaveInputDataShape(subsequentSteps);
    };
    return obj;
  }

  static dataShapeExists(step: Step, input = false): boolean {
    if (input) {
      return step && step.action && step.action.descriptor && step.action.descriptor.inputDataShape !== undefined;
    } else {
      return step && step.action && step.action.descriptor && step.action.descriptor.outputDataShape !== undefined;
    }
  }

  static requiresOutputDataShape(obj: StepKind): StepKind {
    obj.visible = (
      position: number,
      previousSteps: Array<Step>,
      subsequentSteps: Array<Step>
    ) => {
      return StepStore.stepsHaveOutputDataShape(previousSteps);
    };
    return obj;
  }

  static requiresForeach(obj: StepKind): StepKind {
    obj.visible = (
      position: number,
      previousSteps: Array<Step>,
      subsequentSteps: Array<Step>
    ) => {
      return previousSteps.filter(s => s.stepKind == FOREACH).length > 0;
    };
    return obj;
  }

  private getStepConfig(stepKind: String) {
    if (!stepKind) {
      return undefined;
    }
    return this.steps.find(step => step.stepKind === stepKind);
  }

}
