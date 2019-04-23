import {
  Integration,
  createStep,
  Connection,
  Step,
  DataShape,
  Action,
  Flow,
  key,
  ActionDescriptor,
  DataShapeKinds,
  ActionDescriptorStep,
  StringMap,
  ConfigurationProperty,
  createConnectionStep,
  StepOrConnection,
  HIDE_FROM_STEP_SELECT,
} from '@syndesis/ui/platform';
import {
  ENDPOINT,
  StepStore,
  DATA_MAPPER,
  AGGREGATE,
  BASIC_FILTER,
  ADVANCED_FILTER,
  SPLIT,
  TEMPLATE,
} from '@syndesis/ui/store';
import { FlowError, FlowErrorKind } from './edit-page.models';

//
// Various helper functions that the current flow service uses to build an integration object
//

function setFlowId(flow: Flow) {
  return flow.id ? flow : { ...flow, ...{ id: key() } };
}

function setStepId(step: Step) {
  return step.id ? step : { ...step, ...{ id: key() } };
}

/**
 * Validate and add/correct items in a flow's step array
 *  * Sets step IDs
 *  * Strips out invalid/unconfigured steps
 * @param flow
 */
function validateFlowSteps(flow: Flow) {
  return {
    ...flow,
    ...{
      steps: (flow.steps || [])
        .map(setStepId)
        .filter(s => typeof s.stepKind !== 'undefined'),
    },
  };
}

/**
 * Validate all flows and add/correct items as needed
 * @param flows
 */
function validateFlows(flows: Flow[] = []) {
  return flows.map(setFlowId).map(validateFlowSteps);
}

/**
 * Create or enrich the tags for an integration
 * @param flows
 * @param tags
 */
function buildTags(flows: Flow[] = [], tags: string[] = []) {
  const connectorIds = [].concat(
    ...flows.map(f =>
      f.steps
        .filter(step => step.stepKind === ENDPOINT && typeof step.connection !== 'undefined')
        .map(step => step.connection.connectorId)
    )
  );
  return Array.from(new Set([...tags, ...connectorIds]));
}

/**
 * Creates a map of default configured property values for the given properties object
 * @param properties
 */
function getDefaultsFromProperties(
  properties: StringMap<ConfigurationProperty>
) {
  return Object.keys(properties).reduce((result, k) => {
    return { ...result, [k]: properties[k].defaultValue };
  }, {});
}

function getDefaultsForPropertyDefinitionStep(
  propertyDefinitionStep: ActionDescriptorStep
) {
  return getDefaultsFromProperties(propertyDefinitionStep.properties);
}

/**
 * Extracts out the default configuredProperty values for the supplied propertyDefinitionSteps
 * @param propertyDefinitionSteps
 */
function getPropertyDefaults(
  propertyDefinitionSteps: ActionDescriptorStep[] = []
) {
  return propertyDefinitionSteps
    .map(getDefaultsForPropertyDefinitionStep)
    .reduce((result, current) => {
      return { ...result, ...current };
    }, {});
}

/**
 * Inspects the supplied data shape and determines if the user set it on the step or not
 * @param dataShape
 */
export function isUserDefinedDataShape(dataShape: DataShape) {
  return (
    dataShape && dataShape.metadata && dataShape.metadata.userDefined === 'true'
  );
}

/**
 * Checks if the supplied step has either an input or output data shape
 * @param step
 * @param isInput
 */
export function hasDataShape(step: Step, isInput = false) {
  if (!step.action || !step.action.descriptor) {
    return false;
  }
  const descriptor = step.action.descriptor;
  const dataShape = isInput
    ? descriptor.inputDataShape
    : descriptor.outputDataShape;
  return dataShape && dataShape.kind !== DataShapeKinds.NONE;
}

/**
 * Returns whether or not the supplied descriptor has an input or output datashape of ANY
 * @param descriptor
 */
export function isActionShapeless(descriptor: ActionDescriptor) {
  if (!descriptor) {
    return false;
  }
  const inputDataShape = descriptor.inputDataShape;
  const outputDataShape = descriptor.outputDataShape;
  return (
    inputDataShape.kind === DataShapeKinds.ANY ||
    outputDataShape.kind === DataShapeKinds.ANY
  );
}

/**
 * Returns whether or not the supplied descriptor has an input datashape of ANY
 * @param descriptor
 */
export function isActionInputShapeless(descriptor: ActionDescriptor) {
  if (!descriptor) {
    return false;
  }
  const inputDataShape = descriptor.inputDataShape;
  return inputDataShape.kind === DataShapeKinds.ANY;
}

/**
 * Returns whether or not the supplied descriptor has an output datashape of ANY
 * @param descriptor
 */
export function isActionOutputShapeless(descriptor: ActionDescriptor) {
  if (!descriptor) {
    return false;
  }
  const outputDataShape = descriptor.outputDataShape;
  return outputDataShape.kind === DataShapeKinds.ANY;
}

/**
 * Filters connections based on the supplied position in the step array
 * @param steps
 * @param position
 */
export function filterStepsByPosition(
  integration: Integration,
  flowId: string,
  steps: StepOrConnection[],
  position: number
) {
  if (typeof position === 'undefined' || !steps) {
    // safety net
    return steps;
  }
  const atStart = position === 0;
  const atEnd = getLastPosition(integration, flowId) === position;
  return steps.filter((step: any) => {
    // Hide steps that are marked as such, and specifically the log connection
    if (
      (typeof step.connection !== 'undefined' &&
        typeof step.connection.metadata !== 'undefined' &&
        step.connection.metadata[HIDE_FROM_STEP_SELECT]) ||
      (typeof step.metadata !== 'undefined' &&
        step.metadata[HIDE_FROM_STEP_SELECT]) ||
      step.connectorId === 'log'
    ) {
      return false;
    }
    // Special handling for the beginning of a flow
    if (atStart) {
      // At the moment only endpoints can be at the start
      if ('stepKind' in step) {
        return false;
      }
      if (!('connector' in step)) {
        // it's not a connection
        return true;
      }
      return step.connector.actions.some(action => {
        return action.pattern === 'From';
      });
    }
    // Special handling for the end of a flow
    if (atEnd) {
      // Several step kinds aren't usable at the end of a flow
      switch ((step as Step).stepKind) {
        case DATA_MAPPER:
        case BASIC_FILTER:
        case ADVANCED_FILTER:
        case SPLIT:
        case AGGREGATE:
        case TEMPLATE:
          return false;
        default:
      }
    }
    if ((step as Connection).connectorId === 'api-provider') {
      // api provider can be used only for From actions
      return false;
    }
    // All non-connection steps can be shown, except the above
    if ('stepKind' in step && (step as Step).stepKind !== ENDPOINT) {
      return true;
    }
    // Only show connections that have at least one action that accepts data
    return (step as Connection).connector.actions.some(action => {
      return action.pattern === 'To';
    });
  });
}

/**
 * Sets an arbitrary property on an integration
 * @param integration
 * @param propertyName
 * @param value
 */
export function setIntegrationProperty(
  integration: Integration,
  propertyName: string,
  value: any
) {
  if (!propertyName) {
    return integration;
  }
  return { ...integration, ...{ [propertyName]: value } };
}

/**
 * Creates a step object using the supplied connection
 * @param connection
 */
export function createStepWithConnection(connection: Connection) {
  return { ...createStep(), ...{ stepKind: ENDPOINT, connection: connection } };
}

/**
 * Adds the supplied metadata to the step, adding to any existing metadata
 * @param step
 * @param metadata
 */
export function addMetadataToStep(step: Step, metadata: StringMap<any>) {
  if (!metadata || !step) {
    return step;
  }
  const combinedMetadata = { ...step.metadata, ...metadata };
  return { ...step, metadata: combinedMetadata };
}

/**
 * Sets the datashape on a step in the specified direction, preserving any existing configuration
 * @param step
 * @param dataShape
 * @param isInput
 */
export function setDataShapeOnStep(
  step: Step,
  dataShape: DataShape,
  isInput: boolean
) {
  if (!step || !dataShape) {
    return step;
  }
  const prop = isInput ? 'inputDataShape' : 'outputDataShape';
  const action = step.action ? { ...step.action } : ({} as Action);
  const descriptor = { ...action.descriptor, ...{ [prop]: dataShape } };
  return {
    ...step,
    ...{ action: { ...action, ...{ descriptor: { ...descriptor } } } },
  };
}

/**
 * Sets the configured properties on the step
 * @param step
 * @param configuredProperties
 */
export function setConfiguredPropertiesOnStep(
  step: Step,
  configuredProperties: StringMap<any>
) {
  return {
    ...step,
    configuredProperties: stringifyValues(configuredProperties),
  };
}

/**
 * Sets the action on the given step if it's not set or different
 * @param step
 * @param action
 * @param stepKind
 */
export function setActionOnStep(
  step: Step,
  action: Action,
  stepKind: string = ENDPOINT
) {
  // if the step has an action, only set it if the new action is different
  if (step.action && step.action.id === action.id) {
    return step;
  }
  return { ...step, stepKind, action };
}

/**
 * Sets the descriptor on the step, preserving user defined data shapes and setting any config defaults
 * @param step
 * @param descriptor
 */
export function setDescriptorOnStep(
  step: Step,
  descriptor: ActionDescriptor
): Step {
  if (!step) {
    return step;
  }
  // If the step doesn't have an action but we're trying to set a data shape on it, it's probably an extension or something
  if (!step.action) {
    return {
      ...step,
      action: { actionType: 'step', descriptor } as Action,
    };
  }
  const propertyDefaults = getPropertyDefaults(
    descriptor.propertyDefinitionSteps
  );
  // Technically this shouldn't actually be a condition, but for safety's sake...
  if (!step.action.descriptor) {
    return {
      configuredProperties: propertyDefaults,
      ...step,
      action: { ...step.action, descriptor },
    };
  }
  // Update the step's configured properties with any defaults in the descriptor
  const configuredProperties = {
    ...(propertyDefaults || {}),
    ...(step.configuredProperties || {}),
  };
  const oldDescriptor = { ...step.action.descriptor };
  const oldInputDataShape = oldDescriptor.inputDataShape;
  const oldOutputDataShape = oldDescriptor.outputDataShape;
  const preserveInput =
    isUserDefinedDataShape(oldInputDataShape) ||
    (descriptor.inputDataShape.kind !== DataShapeKinds.NONE &&
      !descriptor.inputDataShape.specification);
  const preserveOutput =
    isUserDefinedDataShape(oldOutputDataShape) ||
    (descriptor.outputDataShape.kind !== DataShapeKinds.NONE &&
      !descriptor.outputDataShape.specification);
  return {
    ...step,
    configuredProperties,
    action: {
      ...step.action,
      descriptor: {
        ...descriptor,
        inputDataShape: preserveInput
          ? oldInputDataShape
          : descriptor.inputDataShape,
        outputDataShape: preserveOutput
          ? oldOutputDataShape
          : descriptor.outputDataShape,
      },
    },
  };
}

/**
 * Prepare the configuration for the backend which supports only numbers and strings
 * @param configuredProperties
 */
export function stringifyValues(configuredProperties: any) {
  if (!configuredProperties) {
    return configuredProperties;
  }
  return Object.keys(configuredProperties).reduce((props, name) => {
    const value = configuredProperties[name];
    switch (typeof value) {
      case 'string':
      case 'number':
        return { ...props, [name]: value };
      default:
        return { ...props, [name]: JSON.stringify(value) };
    }
  }, {});
}

/**
 * Performs final checks and tweaks to the integration object, use before posting to the backend
 * @param integration
 */
export function prepareIntegrationForSaving(integration: Integration) {
  const flows = validateFlows(integration.flows);
  const tags = buildTags(integration.flows, integration.tags);
  return { ...integration, tags, flows };
}

/**
 * Returns the flow object with the given ID
 * @param integration
 * @param flowId
 */
export function getFlow(integration: Integration, flowId: string) {
  // TODO some code relies on these semantics
  if (!integration || !integration.flows || !flowId) {
    return undefined;
  }
  return integration.flows.find(flow => flow.id === flowId);
}

/**
 * Returns a new integration object, adding or replacing the supplied flow
 * @param integration
 * @param flow
 */
export function setFlow(integration: Integration, flow: Flow) {
  if (getFlow(integration, flow.id)) {
    return {
      ...integration,
      flows: integration.flows.map(f => {
        if (f.id === flow.id) {
          return flow;
        }
        return f;
      }),
    };
  } else {
    return { ...integration, flows: [...integration.flows, flow] };
  }
}

/**
 * Inserts the supplied step into the indicated flow after the given position
 * @param integration
 * @param flowId
 * @param step
 * @param position
 */
export function insertStepIntoFlowAfter(
  integration: Integration,
  flowId: string,
  step: Step,
  position: number
) {
  const flow = getFlow(integration, flowId);
  const steps = insertStepAfter(flow.steps, step, position);
  return setFlow(integration, { ...flow, steps });
}

/**
 * Inserts the supplied step into the indicated flow before the given position
 * @param integration
 * @param flowId
 * @param step
 * @param position
 */
export function insertStepIntoFlowBefore(
  integration: Integration,
  flowId: string,
  step: Step,
  position: number
) {
  const flow = getFlow(integration, flowId);
  const steps = insertStepBefore(flow.steps, step, position);
  return setFlow(integration, { ...flow, steps });
}

/**
 * Overwrites the supplied step at the given position into the flow
 * @param integration
 * @param flowId
 * @param step
 * @param position
 */
export function setStepInFlow(
  integration: Integration,
  flowId: string,
  step: Step,
  position: number
) {
  const flow = getFlow(integration, flowId);
  const steps = [...flow.steps];
  if (typeof step.id === 'undefined') {
    step.id = key();
  }
  steps[position] = { ...step };
  return setFlow(integration, { ...flow, steps });
}

/**
 * Removes the step at the given position in the supplied flow
 * @param integration
 * @param flowId
 * @param position
 */
export function removeStepFromFlow(
  integration: Integration,
  flowId: string,
  position: number
) {
  const flow = getFlow(integration, flowId);
  const steps = [...flow.steps];
  if (
    position === getFirstPosition(integration, flowId) ||
    position === getLastPosition(integration, flowId)
  ) {
    steps[position] = createStep();
    steps[position].stepKind = ENDPOINT;
  } else {
    steps.splice(position, 1);
  }
  return setFlow(integration, { ...flow, steps });
}

/**
 * Inserts the supplied step after the position
 * @param steps
 * @param step
 * @param position
 */
export function insertStepAfter(steps: Step[], step: Step, position: number) {
  return insertStepBefore(steps, step, position + 1);
}

/**
 * Inserts the supplied step before the position
 * @param steps
 * @param step
 * @param position
 */
export function insertStepBefore(steps: Step[], step: Step, position: number) {
  return [].concat(...steps.slice(0, position), step, ...steps.slice(position));
}

/**
 * Creates a new step, using any default values supplied for the given step kind
 * @param store
 * @param stepKind
 */
export function createStepUsingStore(store: StepStore, stepKind?: string) {
  const stepConfig = store.getDefaultStepDefinition(stepKind);
  return { ...createStep(), ...stepConfig, ...{ id: key(), stepKind } };
}

/**
 * Creates a new empty flow object
 * @param id
 */
export function createFlowWithId(id: string) {
  return {
    id: id,
    steps: [createConnectionStep(), createConnectionStep()],
  } as Flow;
}

/**
 * Returns the start of the step array or undefined if it's not set up
 * @param integration
 * @param flowId
 */
export function getFirstPosition(integration: Integration, flowId: string) {
  if (!flowId) {
    return undefined;
  }
  const flow = getFlow(integration, flowId);
  // TODO preserving semantics for some legacy code
  return typeof flow.steps !== 'undefined' ? 0 : undefined;
}

/**
 * Returns the last index of the step array of the given flow or undefined if it hasn't been created
 * @param integration
 * @param flowId
 */
export function getLastPosition(integration: Integration, flowId: string) {
  if (!flowId) {
    return undefined;
  }
  const flow = getFlow(integration, flowId);
  // TODO preserve this block for now
  if (!flow.steps) {
    return undefined;
  }
  if (flow.steps.length <= 1) {
    return 1;
  }
  return flow.steps.length - 1;
}

/**
 * Returns an index in between the first and last step of the given flow
 * @param integration
 * @param flowId
 */
export function getMiddlePosition(integration: Integration, flowId: string) {
  const position = getLastPosition(integration, flowId);
  return typeof position !== 'undefined' ? Math.round(position / 2) : undefined;
}

/**
 * Returns a copy of the step in the desired flow at the given position
 * @param integration
 * @param flowId
 * @param position
 */
export function getStep(
  integration: Integration,
  flowId: string,
  position: number
) {
  const flow = getFlow(integration, flowId);
  if (!flow) {
    // TODO following semantics for now, this should throw an error
    return undefined;
  }
  const step = flow.steps[position];
  return typeof step !== 'undefined' ? { ...step } : undefined;
}

/**
 * Returns a copy of the first step in the flow
 * @param integration
 * @param flowId
 */
export function getStartStep(integration: Integration, flowId: string) {
  return getStep(integration, flowId, getFirstPosition(integration, flowId));
}

/**
 * Returns a copy of the last step in the flow
 * @param integration
 * @param flowId
 */
export function getLastStep(integration: Integration, flowId: string) {
  return getStep(integration, flowId, getLastPosition(integration, flowId));
}

/**
 * Get an array of middle steps from the given flow, or an empty array if there's only a start/end step
 * @param integration
 * @param flowId
 */
export function getMiddleSteps(integration: Integration, flowId: string) {
  if (getLastPosition(integration, flowId) < 2) {
    // TODO there's no middle steps maybe this should be undefined but following current semantics for now
    return [];
  }
  const flow = getFlow(integration, flowId);
  if (!flow || !flow.steps) {
    // TODO following semantics for now, this should be an error
    return [];
  }
  return flow.steps.slice(1, -1);
}

/**
 * Get an array of steps from the flow after the given position
 * @param integration
 * @param flowId
 * @param position
 */
export function getSubsequentSteps(
  integration: Integration,
  flowId: string,
  position: number
) {
  const flow = getFlow(integration, flowId);
  if (!flow.steps) {
    // TODO following semantics for now, this should throw an error
    return undefined;
  }
  return flow.steps.slice(position + 1);
}

/**
 * Get an array of steps from the flow before the given position
 * @param integration
 * @param flowId
 * @param position
 */
export function getPreviousSteps(
  integration: Integration,
  flowId: string,
  position: number
) {
  const flow = getFlow(integration, flowId);
  if (!flow.steps) {
    // TODO following semantics for now, this should throw an error
    return undefined;
  }
  return flow.steps.slice(0, position);
}

/**
 * Returns all connections after the specified position
 * @param integration
 * @param flowId
 * @param position
 */
export function getSubsequentConnections(
  integration: Integration,
  flowId: string,
  position: number
) {
  const steps = getSubsequentSteps(integration, flowId, position);
  if (steps) {
    return steps.filter(s => s.stepKind === ENDPOINT);
  }
  // TODO this seems like an odd thing to do, but preserving semantics for now
  return null;
}

/**
 * Return all connections before the given position
 * @param integration
 * @param flowId
 * @param position
 */
export function getPreviousConnections(
  integration: Integration,
  flowId: string,
  position: number
) {
  const steps = getPreviousSteps(integration, flowId, position);
  if (steps) {
    return steps.filter(s => s.stepKind === ENDPOINT);
  }
  // TODO this seems like an odd thing to do, but preserving semantics for now
  return null;
}

/**
 * Return the first connection before the given position
 * @param integration
 * @param flowId
 * @param position
 */
export function getPreviousConnection(
  integration: Integration,
  flowId: string,
  position: number
) {
  return (
    getPreviousConnections(integration, flowId, position) || []
  ).reverse()[0];
}

/**
 * Return the first connection after the given position
 * @param integration
 * @param flowId
 * @param position
 */
export function getSubsequentConnection(
  integration: Integration,
  flowId: string,
  position: number
) {
  return (getSubsequentConnections(integration, flowId, position) || [])[0];
}

/**
 * Returns an array of all steps after the given position that contain a data shape
 * @param integration
 * @param flowId
 * @param position
 */
export function getSubsequentStepsWithDataShape(
  integration: Integration,
  flowId: string,
  position: number
): Array<{ step: Step; index: number }> {
  const steps = getSubsequentSteps(integration, flowId, position);
  if (steps) {
    return steps
      .map((step, index) => {
        return { step, index: position + index };
      })
      .filter(indexedStep => hasDataShape(indexedStep.step, true));
  }
  // TODO preserving semantics for now
  return [];
}

/**
 * Return all steps before the given position that have a data shape
 * @param integration
 * @param flowId
 * @param position
 */
export function getPreviousStepsWithDataShape(
  integration: Integration,
  flowId: string,
  position: number
): Array<{ step: Step; index: number }> {
  const steps = getPreviousSteps(integration, flowId, position);
  if (steps) {
    return steps
      .map((step, index) => {
        return { step, index };
      })
      .filter(indexedStep => hasDataShape(indexedStep.step, false));
  }
  // TODO preserving semantics for now
  return [];
}

/**
 * Returns the index of the previous step that has a data shape
 * @param integration
 * @param flowId
 * @param position
 */
export function getPreviousStepIndexWithDataShape(
  integration: Integration,
  flowId: string,
  position: number
) {
  const steps = getPreviousStepsWithDataShape(integration, flowId, position);
  if (steps && steps.length) {
    return steps.reverse()[0].index;
  }
  return -1;
}

/**
 * Returns the first previous step that has a data shape
 * @param integration
 * @param flowId
 * @param position
 */
export function getPreviousStepWithDataShape(
  integration: Integration,
  flowId: string,
  position: number
) {
  const steps = getPreviousStepsWithDataShape(integration, flowId, position);
  if (steps && steps.length) {
    return steps.reverse()[0].step;
  }
  return undefined;
}

/**
 * Returns the next step after the given position that contains a data shape
 * @param integration
 * @param flowId
 * @param position
 */
export function getSubsequentStepWithDataShape(
  integration: Integration,
  flowId: string,
  position: number
) {
  const steps = getSubsequentStepsWithDataShape(integration, flowId, position);
  if (steps && steps.length) {
    return steps[0].step;
  }
  return undefined;
}

/**
 * Inspects the indicated flow by ID and returns an array of possible issues with it
 * @param integration
 * @param flowId
 */
export function validateFlow(
  integration: Integration,
  flowId: string
): FlowError[] {
  const errors: FlowError[] = [];
  if (!flowId) {
    throw new Error('Invalid flow ID specified');
  }
  if (!integration) {
    throw new Error('Invalid integration object given');
  }
  const startStep = getStartStep(integration, flowId);
  if (
    typeof startStep === 'undefined' ||
    typeof startStep.stepKind === 'undefined' ||
    (typeof startStep.connection === 'undefined' &&
      startStep.stepKind === ENDPOINT)
  ) {
    errors.push({ kind: FlowErrorKind.NO_START_CONNECTION });
  }
  const endStep = getLastStep(integration, flowId);
  if (
    typeof endStep === 'undefined' ||
    typeof endStep.stepKind === 'undefined' ||
    (endStep.stepKind === ENDPOINT &&
      typeof endStep.connection === 'undefined')
  ) {
    errors.push({ kind: FlowErrorKind.NO_FINISH_CONNECTION });
  }
  return errors;
}

/**
 * Finds the closest step of type 'Aggregate' after the provided position.
 * @param integration
 * @param flowId
 * @param position
 */
export function getNextAggregateStep(
  integration: Integration,
  flowId: string,
  position: number
): Step | undefined {
  const steps = getSubsequentSteps(integration, flowId, position);
  if (steps && steps.length) {
    return steps.filter(s => s.stepKind === AGGREGATE)[0];
  }
  return undefined;
}
