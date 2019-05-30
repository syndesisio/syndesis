/* tslint:disable:object-literal-sort-keys */
import { Step, StepKind } from '@syndesis/models';
import {
  ADVANCED_FILTER,
  AGGREGATE,
  BASIC_FILTER,
  DATA_MAPPER,
  DataShapeKinds,
  LOG,
  SPLIT,
  TEMPLATE,
} from './constants';
import { toDataShapeKinds } from './helpers';

export const ALL_STEPS: StepKind[] = [
  requiresInputDataShape({
    id: undefined,
    connection: undefined,
    action: undefined,
    name: 'Data Mapper',
    description: 'Map fields from the input type to the output type.',
    stepKind: DATA_MAPPER,
    properties: {},
    configuredProperties: undefined,
  }),
  requiresOutputDataShape(
    {
      id: undefined,
      connection: undefined,
      action: undefined,
      name: 'Basic Filter',
      description:
        'Continue the integration only if criteria you specify in simple input fields are met. Suitable for' +
        ' most integrations.',
      stepKind: BASIC_FILTER,
      properties: undefined,
      configuredProperties: undefined,
    },
    true
  ),
  {
    id: undefined,
    connection: undefined,
    action: undefined,
    name: 'Template',
    stepKind: TEMPLATE,
    description:
      'Upload or create a Freemarker, Mustache or Velocity template to define consistent output data.',
    configuredProperties: undefined,
    properties: undefined,
  },
  noCollectionSupport({
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
        rows: 10,
      },
    },
    configuredProperties: undefined,
  }),
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
        required: false,
      },
      bodyLoggingEnabled: {
        type: 'boolean',
        displayName: 'Message Body',
        required: false,
      },
      customText: {
        type: 'string',
        displayName: 'Custom Text',
        required: false,
      },
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
    },
  },
  requiresOutputDataShape({
    id: undefined,
    connection: undefined,
    action: undefined,
    name: 'Split',
    description: 'Process each item in a set of data individually',
    stepKind: SPLIT,
    properties: {},
    configuredProperties: undefined,
  }),
  requiresConsistentSplitAggregate({
    id: undefined,
    connection: undefined,
    action: undefined,
    name: 'Aggregate',
    description: 'End processing items in a foreach',
    stepKind: AGGREGATE,
    properties: {},
    configuredProperties: undefined,
  }),
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

function stepsHaveOutputDataShape(steps: Step[]): boolean {
  return (
    steps.filter(
      s =>
        s &&
        s.action &&
        s.action.descriptor &&
        s.action.descriptor.outputDataShape &&
        s.action.descriptor.outputDataShape.kind &&
        toDataShapeKinds(s.action.descriptor.outputDataShape.kind) !==
          DataShapeKinds.NONE &&
        toDataShapeKinds(s.action.descriptor.outputDataShape.kind) !==
          DataShapeKinds.ANY
    ).length > 0
  );
}

function stepsHaveInputDataShape(steps: Step[]): boolean {
  return (
    steps.filter(
      s =>
        s &&
        s.action &&
        s.action.descriptor &&
        s.action.descriptor.inputDataShape &&
        s.action.descriptor.inputDataShape.kind &&
        toDataShapeKinds(s.action.descriptor.inputDataShape.kind) !==
          DataShapeKinds.NONE &&
        toDataShapeKinds(s.action.descriptor.inputDataShape.kind) !==
          DataShapeKinds.ANY
    ).length > 0
  );
}

// currently no steps fit this criteria but that could change
// @ts-ignore
function requiresInputOutputDataShapes(
  obj: StepKind,
  anyPrevious = true,
  anySubsequent = true
): StepKind {
  obj.visible = (
    position: number,
    previousSteps: Step[],
    subsequentSteps: Step[]
  ) => {
    if (!anyPrevious) {
      // only test the first previous step that has some kind of data shape
      const previousStep = previousSteps
        .reverse()
        .find(s => dataShapeExists(s));
      previousSteps = previousStep ? [previousStep] : [];
    }
    if (!anySubsequent) {
      // only test the next subsequent step that has a data shape
      const subsequentStep = subsequentSteps.find(s =>
        dataShapeExists(s, true)
      );
      subsequentSteps = subsequentStep ? [subsequentStep] : [];
    }
    return (
      stepsHaveOutputDataShape(previousSteps) &&
      stepsHaveInputDataShape(subsequentSteps)
    );
  };
  return obj;
}

function dataShapeExists(step: Step, input = false): boolean {
  if (input) {
    return !!(
      step &&
      step.action &&
      step.action.descriptor &&
      step.action.descriptor.inputDataShape
    );
  } else {
    return !!(
      step &&
      step.action &&
      step.action.descriptor &&
      step.action.descriptor.outputDataShape
    );
  }
}

function hasPrecedingCollection(previousSteps: Step[]) {
  const previousDataShape = previousSteps
    .reverse()
    .find(s => dataShapeExists(s));
  return (
    previousDataShape &&
    previousDataShape.action &&
    previousDataShape.action.descriptor &&
    previousDataShape.action.descriptor.outputDataShape &&
    previousDataShape.action.descriptor.outputDataShape.metadata &&
    previousDataShape.action.descriptor.outputDataShape.metadata.variant ===
      'collection'
  );
}

function noCollectionSupport(obj: StepKind) {
  obj.visible = (
    position: number,
    previousSteps: Step[],
    subsequentSteps: Step[]
  ) => {
    return !hasPrecedingCollection(previousSteps);
  };
  return obj;
}

function requiresOutputDataShape(
  obj: StepKind,
  noCollectionSupportP = false
): StepKind {
  if (noCollectionSupportP) {
    obj.visible = (
      position: number,
      previousSteps: Step[],
      subsequentSteps: Step[]
    ) => {
      return (
        stepsHaveOutputDataShape(previousSteps) &&
        !hasPrecedingCollection(previousSteps)
      );
    };
  } else {
    obj.visible = (
      position: number,
      previousSteps: Step[],
      subsequentSteps: Step[]
    ) => {
      return stepsHaveOutputDataShape(previousSteps);
    };
  }
  return obj;
}

function requiresInputDataShape(
  obj: StepKind,
  noCollectionSupportP = false
): StepKind {
  if (noCollectionSupportP) {
    obj.visible = (
      position: number,
      previousSteps: Step[],
      subsequentSteps: Step[]
    ) => {
      return (
        stepsHaveInputDataShape(subsequentSteps) &&
        !hasPrecedingCollection(previousSteps)
      );
    };
  } else {
    obj.visible = (
      position: number,
      previousSteps: Step[],
      subsequentSteps: Step[]
    ) => {
      return stepsHaveInputDataShape(subsequentSteps);
    };
  }
  return obj;
}

function requiresConsistentSplitAggregate(obj: StepKind): StepKind {
  obj.visible = (
    position: number,
    previousSteps: Step[],
    subsequentSteps: Step[]
  ) => {
    const countPreviousSplit = previousSteps.filter(s => s.stepKind === SPLIT)
      .length;
    const countPreviousAggregate = previousSteps.filter(
      s => (s.stepKind || '').toLowerCase() === AGGREGATE
    ).length;

    if (countPreviousSplit <= countPreviousAggregate) {
      return false;
    }

    const positionNextSplit = subsequentSteps.findIndex(
      s => s.stepKind === SPLIT
    );
    const positionNextAggregate = subsequentSteps.findIndex(
      s => s.stepKind === AGGREGATE
    );

    if (positionNextSplit === -1) {
      return positionNextAggregate === -1;
    }

    return (
      positionNextAggregate === -1 || positionNextSplit < positionNextAggregate
    );
  };
  return obj;
}

export interface IStepsResponse {
  items: StepKind[];
}

export interface IWithStepsProps {
  disableUpdates?: boolean;
  children(props: IStepsResponse): any;
}

export const WithSteps: React.FunctionComponent<IWithStepsProps> = ({
  children,
}) => {
  return children({
    items: ALL_STEPS,
  });
};
