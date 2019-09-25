import {
  Action,
  ActionDescriptor,
  ActionDescriptorStep,
  ConfigurationProperty,
  Connection,
  DataShape,
  Flow,
  IndexedStep,
  Integration,
  IntegrationOverview,
  Step,
  StepKind,
  StringMap,
} from '@syndesis/models';
import { key as generateKey } from '@syndesis/utils';
import produce from 'immer';
import {
  AGGREGATE,
  API_PROVIDER,
  CHOICE,
  DataShapeKinds,
  DataShapeKindType,
  ENDPOINT,
  EXCERPT_METADATA_KEY,
  FLOW,
  FLOW_KIND_METADATA_KEY,
  FlowKind,
  FlowType,
  NEW_INTEGRATION,
  NEW_INTEGRATION_ID,
  PRIMARY_FLOW_ID_METADATA_KEY,
  STEP_ID_METADATA_KEY,
} from '../constants';

export function toDataShapeKindType(kind?: DataShapeKinds): DataShapeKindType {
  return kind!.toLowerCase() as DataShapeKindType;
}

export function toDataShapeKinds(
  kind?:
    | 'ANY'
    | 'JAVA'
    | 'JSON_SCHEMA'
    | 'JSON_INSTANCE'
    | 'XML_SCHEMA'
    | 'XML_SCHEMA_INSPECTED'
    | 'XML_INSTANCE'
    | 'NONE'
    | string
) {
  switch ((kind! as string).toLowerCase()) {
    case 'any':
      return DataShapeKinds.ANY;
    case 'java':
      return DataShapeKinds.JAVA;
    case 'json_schema':
    case 'json-schema':
      return DataShapeKinds.JSON_SCHEMA;
    case 'json_instance':
    case 'json-instance':
      return DataShapeKinds.JSON_INSTANCE;
    case 'xml_schema':
    case 'xml-schema':
      return DataShapeKinds.XML_SCHEMA;
    case 'xml_schema_inspected':
    case 'xml-schema-inspected':
      return DataShapeKinds.XML_SCHEMA_INSPECTED;
    case 'xml_instance':
    case 'xml-instance':
      return DataShapeKinds.XML_INSTANCE;
    case 'none':
      return DataShapeKinds.NONE;
    default:
      throw new Error(`Invalid data shape kind: ${kind}`);
  }
}

/**
 * returns an empty integration object.
 *
 */
export function getEmptyIntegration(): Integration {
  return produce(NEW_INTEGRATION, draft => {
    draft.flows = [
      {
        id: generateKey(),
        name: '',
        steps: [],
      },
    ];
  });
}

export function setIntegrationProperties(
  integration: Integration,
  properties: StringMap<any>
): Integration {
  return produce(integration, nextIntegration => {
    Object.keys(properties).forEach(k => {
      nextIntegration[k] = properties[k];
    });
  });
}

/**
 * returns true if the provided integration can be published; returns false
 * otherwise.
 *
 * @param integration
 */
export function canPublish(integration: IntegrationOverview) {
  return integration.currentState !== 'Pending';
}

/**
 * returns true if the provided integration can be activated; returns false
 * otherwise.
 *
 * @param integration
 */
export function canActivate(integration: IntegrationOverview) {
  return (
    integration.currentState !== 'Pending' &&
    integration.currentState !== 'Published'
  );
}

/**
 * returns true if the provided integration can be edited; returns false
 * otherwise.
 *
 * @param integration
 */
export function canEdit(integration: IntegrationOverview) {
  return integration.currentState !== 'Pending';
}

/**
 * returns true if the provided integration can be deactivated; returns false
 * otherwise.
 *
 * @param integration
 */
export function canDeactivate(integration: IntegrationOverview) {
  return integration.currentState !== 'Unpublished';
}

/**
 * returns the list of steps of the provided integration.
 *
 * @param integration
 * @param flowId
 *
 * @todo make the returned object immutable to avoid uncontrolled changes
 */
export function getSteps(integration: Integration, flowId: string): Step[] {
  try {
    const flow = getFlow(integration, flowId);
    return flow!.steps!;
  } catch (e) {
    return [];
  }
}

//
// Various helper functions that the current flow service uses to build an integration object
//

function setFlowId(flow: Flow) {
  return flow.id ? flow : { ...flow, ...{ id: generateKey() } };
}

export function setStepId(step: Step) {
  return step.id ? step : { ...step, ...{ id: generateKey() } };
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
  const connectorIds = ([] as string[]).concat(
    ...flows.map(f =>
      f
        .steps!.filter(
          step =>
            step.stepKind === ENDPOINT && typeof step.connection !== 'undefined'
        )
        .map(step => step.connection!.connectorId!)
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
  return getDefaultsFromProperties(propertyDefinitionStep.properties!);
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
export function isUserDefinedDataShape(dataShape?: DataShape) {
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
  return (
    dataShape &&
    dataShape.kind &&
    toDataShapeKinds(dataShape.kind) !== DataShapeKinds.NONE
  );
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
    inputDataShape &&
    outputDataShape &&
    (inputDataShape.kind && outputDataShape.kind) &&
    (toDataShapeKinds(inputDataShape.kind) === DataShapeKinds.ANY ||
      toDataShapeKinds(outputDataShape.kind) === DataShapeKinds.ANY)
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
  return (
    inputDataShape &&
    inputDataShape.kind &&
    toDataShapeKinds(inputDataShape.kind) === DataShapeKinds.ANY
  );
}

export function requiresOutputDescribeDataShape(descriptor: ActionDescriptor) {
  if (!descriptor) {
    return false;
  }
  if (isActionOutputShapeless(descriptor)) {
    return true;
  }
  return isUserDefinedDataShape(descriptor.outputDataShape);
}

export function requiresInputDescribeDataShape(descriptor: ActionDescriptor) {
  if (!descriptor) {
    return false;
  }
  if (isActionInputShapeless(descriptor)) {
    return true;
  }
  return isUserDefinedDataShape(descriptor.inputDataShape);
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
  return (
    outputDataShape &&
    outputDataShape.kind &&
    toDataShapeKinds(outputDataShape.kind) === DataShapeKinds.ANY
  );
}

/**
 * Converts the response body or status message into a Syndesis error exception
 * @param response
 */
export async function throwStandardError(response: Response) {
  const text = await response.text();
  let json;
  try {
    json = JSON.parse(text);
  } catch (e) {
    throw { errorCode: response.status, userMsg: text || response.statusText };
  }
  throw json;
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

export function createStep(): Step {
  return { id: generateKey() };
}

export function createConnectionStep(): Step {
  const step = createStep();
  step.stepKind = 'endpoint';
  return step;
}

/**
 * Creates a step object using the supplied connection
 * @param connection
 */
export function createStepWithConnection(connection: Connection) {
  return { ...createStep(), ...{ stepKind: ENDPOINT, connection } };
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
  return {
    ...step,
    action: applyUserDefinedDataShapesToAction(step.action, {
      ...step.action,
      descriptor,
    }),
    configuredProperties,
  };
}

/**
 * Copies user-defined data shapes from an old copy to a new copy of the action
 * @param oldAction
 * @param newAction
 */
export function applyUserDefinedDataShapesToAction(
  oldAction: Action,
  newAction: Action
) {
  if (!oldAction) {
    return newAction;
  }
  const descriptor = newAction.descriptor!;
  const oldDescriptor = oldAction.descriptor!;
  return setDescriptorOnAction(newAction, oldDescriptor, descriptor);
}

/**
 * Compares the old and new descriptors and returns a new action object
 * with user defined data shapes preserved
 * @param action
 * @param oldDescriptor
 * @param descriptor
 */
export function setDescriptorOnAction(
  action: Action,
  oldDescriptor: ActionDescriptor,
  descriptor: ActionDescriptor
): Action {
  const oldInputDataShape = oldDescriptor.inputDataShape;
  const oldOutputDataShape = oldDescriptor.outputDataShape;
  const preserveInput =
    isUserDefinedDataShape(oldInputDataShape) ||
    (descriptor.inputDataShape &&
      descriptor.inputDataShape.kind &&
      (toDataShapeKinds(descriptor.inputDataShape.kind) !==
        DataShapeKinds.NONE &&
        !descriptor.inputDataShape.specification));
  const preserveOutput =
    isUserDefinedDataShape(oldOutputDataShape) ||
    (descriptor.outputDataShape &&
      descriptor.outputDataShape.kind &&
      (toDataShapeKinds(descriptor.outputDataShape.kind) !==
        DataShapeKinds.NONE &&
        !descriptor.outputDataShape.specification));
  return {
    ...action,
    descriptor: {
      ...descriptor,
      inputDataShape: preserveInput
        ? oldInputDataShape
        : descriptor.inputDataShape,
      outputDataShape: preserveOutput
        ? oldOutputDataShape
        : descriptor.outputDataShape,
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
  const { id } = integration;
  const flows = validateFlows(integration.flows);
  const tags = buildTags(integration.flows, integration.tags);
  return {
    ...integration,
    flows,
    id: id === NEW_INTEGRATION_ID ? undefined : id,
    tags,
  };
}

export type GetSanitizedSteps = (steps: Step[]) => Promise<Step[]>;

export async function sanitizeFlow(
  flow: Flow,
  getSanitizedSteps: GetSanitizedSteps
): Promise<Flow> {
  flow.steps = await getSanitizedSteps(flow.steps || []);
  // make sure we have all the connection ids as tags for the flow
  flow.tags = Array.from(
    new Set([
      ...(flow.tags || []),
      ...flow.steps
        .filter(s => s.connection && s.connection.id)
        .map(s => s.connection!.id),
    ])
  ) as string[];
  // Ensure the type is set properly on the flow, if it's not set we assume it's a primary flow
  flow.type = flow.type || FlowType.PRIMARY;
  // for the api provider, if a flow has been modified we change the last
  // step of the flow to automatically set a return code of 200, unless
  // already modified by the user. Also, we update the flow metadata to
  // reflect that the flow has been "implemented"
  const lastStep = flow.steps[flow.steps.length - 1];
  if (
    lastStep &&
    lastStep.action &&
    lastStep.action.id === 'io.syndesis:api-provider-end'
  ) {
    if (
      !lastStep.configuredProperties ||
      (lastStep.configuredProperties &&
        lastStep.configuredProperties.httpResponseCode === '501')
    ) {
      const returnCode = flow.metadata!['default-return-code'];
      const returnCodeEdited = flow.metadata!['return-code-edited'];
      if (returnCode && !returnCodeEdited) {
        flow.metadata!['return-code-edited'] = 'true';
        lastStep.configuredProperties!.httpResponseCode = returnCode;
      }
    }
  }

  return flow;
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
 * @param getSanitizedSteps
 */
export async function setFlow(
  integration: Integration,
  flow: Flow,
  getSanitizedSteps: GetSanitizedSteps
) {
  flow = await sanitizeFlow(flow, getSanitizedSteps);
  if (getFlow(integration, flow.id!)) {
    const updatedIntegration = {
      ...integration,
      flows: integration.flows!.map(f => {
        if (f.id === flow.id) {
          return flow;
        }
        return f;
      }),
    };

    if (isPrimaryFlow(flow)) {
      return reconcileIntegration(updatedIntegration, flow);
    } else {
      return updatedIntegration;
    }
  } else {
    return { ...integration, flows: [...integration.flows!, flow] };
  }
}

/**
 * Returns a new integration object with reconciled flows and steps according to the changes in the updated flow.
 * Reconcile logic includes conditional flow steps to update all linked alternate flows with up-to-date data shapes.
 * @param integration
 * @param updatedFlow
 */
export function reconcileIntegration(
  integration: Integration,
  updatedFlow: Flow) {
  let reconciledIntegration = { ...integration };

  const conditionalFlowsSteps = updatedFlow.steps!.filter(step => step.stepKind === CHOICE);
  for (const cfStep of conditionalFlowsSteps) {
    reconciledIntegration = reconcileConditionalFlows(reconciledIntegration,
      getFlowsWithLinkedStepId(reconciledIntegration.flows!, cfStep.id!),
      cfStep.id!,
      cfStep.action!.descriptor!.inputDataShape!,
      cfStep.action!.descriptor!.outputDataShape!)
  }

  return reconciledIntegration;
}

/**
 * Returns a new integration object containing the supplied alternate flows for the given conditional flows step ID. The
 * given alternate flows are reconciled which includes setting of flow start and end data shapes according to the
 * conditional flow step input and output data shape.
 * @param integration
 * @param alternateFlows all alternate flows linked to the conditional flow step
 * @param stepId the conditional flow step id
 * @param flowStartDataShape the input data shape of the conditional flow step to apply as an output data shape to flow start steps
 * @param flowEndDataShape the output data shape of the conditional flow step to apply as an input data shape to flow end steps
 */
export function reconcileConditionalFlows(
  integration: Integration,
  alternateFlows: Flow[],
  stepId: string,
  flowStartDataShape: DataShape,
  flowEndDataShape: DataShape
) {
  const flowsWithoutStepId = getFlowsWithoutLinkedStepId(
    integration.flows!,
    stepId
  );
  const updatedFlows = alternateFlows.map(flow => {
    const startFlowStep = setDataShapeOnStep(
      { ...flow.steps![0] },
      flowStartDataShape,
      false
    );
    const updatedFlow = applyUpdatedStep(flow, startFlowStep, 0);

    const endStepPosition = getStepsLastPosition(flow.steps!);
    const endFlowStep = setDataShapeOnStep(
      { ...flow.steps![endStepPosition] },
      flowEndDataShape,
      true
    );
    return applyUpdatedStep(updatedFlow, endFlowStep, endStepPosition);
  });
  return { ...integration, flows: [...flowsWithoutStepId, ...updatedFlows] };
}

/**
 * Return a list of flows that are not linked to the given step id. Usually the given
 * step id refers to a conditional flow step and this function returns all flows that are NOT marked as alternate flows
 * according to this step meaning not linked to the step.
 * @param flows
 * @param stepId
 */
export function getFlowsWithoutLinkedStepId(flows: Flow[], stepId: string) {
  return flows.filter(
    flow =>
      flow.type === FlowType.PRIMARY ||
      flow.type === FlowType.API_PROVIDER ||
      getMetadataValue(STEP_ID_METADATA_KEY, flow.metadata) !== stepId
  );
}

/**
 * Returns a list of alternate flows that are linked to the given step id. Usually the given step id refers to a
 * conditional flow step. All alternate flows linked to this step are subject to be returned by this function.
 * @param flows
 * @param stepId
 */
export function getFlowsWithLinkedStepId(flows: Flow[], stepId: string) {
  return flows.filter(
    flow =>
      flow.type === FlowType.ALTERNATE &&
      getMetadataValue(STEP_ID_METADATA_KEY, flow.metadata) === stepId
  );
}

/**
 * Inserts the supplied step into the indicated flow after the given position
 * @param integration
 * @param flowId
 * @param step
 * @param position
 * @param getSanitizedSteps
 */
export function insertStepIntoFlowAfter(
  integration: Integration,
  flowId: string,
  step: Step,
  position: number,
  getSanitizedSteps: GetSanitizedSteps
) {
  const flow = getFlow(integration, flowId);
  const steps = insertStepAfter(flow!.steps!, step, position);
  return setFlow(integration, { ...flow!, steps }, getSanitizedSteps);
}

/**
 * Inserts the supplied step into the indicated flow before the given position
 * @param integration
 * @param flowId
 * @param step
 * @param position
 * @param getSanitizedSteps
 */
export function insertStepIntoFlowBefore(
  integration: Integration,
  flowId: string,
  step: Step,
  position: number,
  getSanitizedSteps: GetSanitizedSteps
) {
  const flow = getFlow(integration, flowId);
  const steps = insertStepBefore(flow!.steps!, step, position);
  return setFlow(integration, { ...flow!, steps }, getSanitizedSteps);
}

/**
 * Overwrites the supplied step at the given position into the flow
 * @param integration
 * @param flowId
 * @param step
 * @param position
 * @param getSanitizedSteps
 */
export function setStepInFlow(
  integration: Integration,
  flowId: string,
  step: Step,
  position: number,
  getSanitizedSteps: GetSanitizedSteps
) {
  const flow = getFlow(integration, flowId);
  return setFlow(
    integration,
    applyUpdatedStep(flow!, step, position),
    getSanitizedSteps
  );
}

/**
 * Returns a new flow object with the supplied step set at the given position
 * @param flow
 * @param step
 * @param position
 */
export function applyUpdatedStep(flow: Flow, step: Step, position: number) {
  const steps = [...flow!.steps!];
  if (typeof step.id === 'undefined') {
    step.id = generateKey();
  }
  steps[position] = { ...step };
  return { ...flow!, steps };
}

/**
 * Removes the step at the given position in the supplied flow
 * @param integration
 * @param flowId
 * @param position
 * @param getSanitizedSteps
 */
export function removeStepFromFlow(
  integration: Integration,
  flowId: string,
  position: number,
  getSanitizedSteps: GetSanitizedSteps
) {
  const flow = getFlow(integration, flowId);
  const steps = getSteps(integration, flowId);
  const toDelete = getStep(integration, flowId, position)!;

  // special handling for conditional flows, related flows need
  // to be removed from the integration as well.
  const flows =
    toDelete.stepKind === CHOICE
      ? getFlowsWithoutLinkedStepId(integration.flows!, toDelete.id!)
      : integration.flows;

  if (
    position === getFirstPosition(integration, flowId) ||
    position === getLastPosition(integration, flowId)
  ) {
    steps[position] = createStep();
    steps[position].stepKind = ENDPOINT;
  } else {
    steps.splice(position, 1);
  }

  return setFlow(
    { ...integration, flows },
    { ...flow!, steps },
    getSanitizedSteps
  );
}

/**
 * Compute choice configuration mode from configured properties. In case any of given flow options uses an expression
 * condition we use advanced config mode otherwise basic.
 * @param step
 */
export function getChoiceConfigMode(step: StepKind) {
  if (step.stepKind === CHOICE &&
      typeof step.configuredProperties!.flows !== 'undefined' &&
      step.configuredProperties!.flows.length > 0) {
    const flows = JSON.parse(step.configuredProperties!.flows) as any[];
    if (flows.find(flow => flow.condition!.length > 0)) {
      return 'advanced';
    } else {
      return 'basic';
    }
  }

  return undefined;
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
  return ([] as Step[]).concat(
    ...steps.slice(0, position),
    step,
    ...steps.slice(position)
  );
}
//
// /**
//  * Creates a new step, using any default values supplied for the given step kind
//  * @param store
//  * @param stepKind
//  */
// export function createStepUsingStore(store: StepStore, stepKind?: string) {
//   const stepConfig = store.getDefaultStepDefinition(stepKind);
//   return { ...createStep(), ...stepConfig, ...{ id: key(), stepKind } };
// }

/**
 * Creates a new empty flow object
 * @param id
 */
export function createFlowWithId(id: string) {
  return {
    id,
    steps: [createConnectionStep(), createConnectionStep()],
  } as Flow;
}

/**
 * Create a new alternate flow and relate it to the supplied step
 * @param name
 * @param description
 * @param kind
 * @param primaryFlowId
 * @param flowConnectionTemplate
 * @param step
 * @param useId - ID to use instead of generating one
 */
export function createConditionalFlow(
  name: string,
  description: string,
  kind: FlowKind,
  primaryFlowId: string,
  flowConnectionTemplate: Connection,
  step: StepKind,
  useId?: string
) {
  if (typeof step.id === 'undefined') {
    throw Error('Cannot create conditional flow for a step with no ID set');
  }
  const flowId = useId || generateKey();
  return {
    connections: [],
    description,
    id: flowId,
    metadata: {
      [EXCERPT_METADATA_KEY]: '',
      [FLOW_KIND_METADATA_KEY]: kind,
      [PRIMARY_FLOW_ID_METADATA_KEY]: primaryFlowId,
      [STEP_ID_METADATA_KEY]: step.id,
    },
    name,
    steps: [
      createConditionalFlowStart(flowId, flowConnectionTemplate, step),
      createConditionalFlowEnd(flowConnectionTemplate),
    ],
    type: FlowType.ALTERNATE,
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
  return typeof flow!.steps !== 'undefined' ? 0 : undefined;
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
  if (!flow!.steps) {
    return undefined;
  }
  if (flow!.steps.length <= 1) {
    return 1;
  }
  return flow!.steps.length - 1;
}

export function getStepsLastPosition(steps: Step[]) {
  if (steps.length <= 1) {
    return 1;
  }
  return steps.length - 1;
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
  const step = flow.steps![position];
  return typeof step !== 'undefined' ? { ...step } : undefined;
}

/**
 * Returns a copy of the first step in the flow
 * @param integration
 * @param flowId
 */
export function getStartStep(integration: Integration, flowId: string) {
  return getStep(integration, flowId, getFirstPosition(integration, flowId)!);
}

/**
 * Returns a copy of the last step in the flow
 * @param integration
 * @param flowId
 */
export function getLastStep(integration: Integration, flowId: string) {
  return getStep(integration, flowId, getLastPosition(integration, flowId)!);
}

/**
 * Get an array of middle steps from the given flow, or an empty array if there's only a start/end step
 * @param integration
 * @param flowId
 */
export function getMiddleSteps(integration: Integration, flowId: string) {
  if (getLastPosition(integration, flowId)! < 2) {
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
export function getIntegrationSubsequentSteps(
  integration: Integration,
  flowId: string,
  position: number
) {
  const steps = getSteps(integration, flowId);
  return getSubsequentSteps(steps, position);
}

/**
 * Get an array of steps from the flow after the given position
 * @param steps
 * @param position
 */
export function getSubsequentSteps(steps: Step[], position: number) {
  return steps.slice(position + 1);
}

/**
 * Get an array of steps from the flow before the given position
 * @param integration
 * @param flowId
 * @param position
 */
export function getPreviousIntegrationSteps(
  integration: Integration,
  flowId: string,
  position: number
) {
  const steps = getSteps(integration, flowId);
  return getPreviousSteps(steps, position);
}
/**
 * Get an array of steps from the flow before the given position
 * @param steps
 * @param position
 */
export function getPreviousSteps(steps: Step[], position: number) {
  return steps.slice(0, position);
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
  const steps = getIntegrationSubsequentSteps(integration, flowId, position);
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
  const steps = getPreviousIntegrationSteps(integration, flowId, position);
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
  const prevConns = getPreviousConnections(integration, flowId, position) || [];
  return prevConns[prevConns.length - 1];
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
export function getSubsequentIntegrationStepsWithDataShape(
  integration: Integration,
  flowId: string,
  position: number
): Array<{ step: Step; index: number }> {
  const steps = getIntegrationSubsequentSteps(integration, flowId, position);
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
export function getPreviousIntegrationStepsWithDataShape(
  integration: Integration,
  flowId: string,
  position: number
): IndexedStep[] {
  const steps = getSteps(integration, flowId);
  return getPreviousStepsWithDataShape(steps || [], position);
}

/**
 * Finds previous step with data shape according to given position in integration flow and returns
 * that output data shape.
 * @param integration
 * @param flowId
 * @param position
 */
export function getOutputDataShapeFromPreviousStep(
  integration: Integration,
  flowId: string,
  position: number
): DataShape {
  let dataShape = {} as DataShape;
  try {
    const prevStep = getPreviousIntegrationStepWithDataShape(
      integration,
      flowId,
      position
    );
    dataShape =
      prevStep!.action!.descriptor!.outputDataShape ||
      ({} as DataShape);
  } catch (err) {
    // ignore
  }

  return dataShape;
}

/**
 * Return all steps before the given position that have a data shape
 * @param steps
 * @param position
 */
export function getPreviousStepsWithDataShape(
  steps: Step[],
  position: number
): Array<{ step: Step; index: number }> {
  const previousSteps = getPreviousSteps(steps, position);
  if (previousSteps) {
    return previousSteps
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
export function getPreviousIntegrationStepIndexWithDataShape(
  integration: Integration,
  flowId: string,
  position: number
) {
  const steps = getPreviousIntegrationStepsWithDataShape(
    integration,
    flowId,
    position
  );
  if (steps && steps.length) {
    return steps[steps.length - 1].index;
  }
  return -1;
}

/**
 * Returns the first previous step that has a data shape
 * @param integration
 * @param flowId
 * @param position
 */
export function getPreviousIntegrationStepWithDataShape(
  integration: Integration,
  flowId: string,
  position: number
) {
  const steps = getSteps(integration, flowId);
  return getPreviousStepWithDataShape(steps || [], position);
}

/**
 * Returns the first previous step that has a data shape
 * @param steps
 * @param position
 */
export function getPreviousStepWithDataShape(steps: Step[], position: number) {
  const previousSteps = getPreviousStepsWithDataShape(steps, position);
  if (previousSteps && previousSteps.length) {
    return previousSteps[previousSteps.length - 1].step;
  }
  return undefined;
}

/**
 * Returns the next step after the given position that contains a data shape
 * @param integration
 * @param flowId
 * @param position
 */
export function getSubsequentIntegrationStepWithDataShape(
  integration: Integration,
  flowId: string,
  position: number
) {
  const steps = getSubsequentIntegrationStepsWithDataShape(
    integration,
    flowId,
    position
  );
  if (steps && steps.length) {
    return steps[0].step;
  }
  return undefined;
}

export interface IFlowEvent {
  kind: string;
  [name: string]: any;
}

export enum FlowErrorKind {
  NO_START_CONNECTION,
  NO_FINISH_CONNECTION,
  NO_NAME,
}

export interface IFlowError {
  kind: FlowErrorKind;
  [name: string]: any;
}

/**
 * Inspects the indicated flow by ID and returns an array of possible issues with it
 * @param integration
 * @param flowId
 */
export function validateFlow(
  integration: Integration,
  flowId: string
): IFlowError[] {
  const errors: IFlowError[] = [];
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
    (endStep.stepKind === ENDPOINT && typeof endStep.connection === 'undefined')
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
export function getIntegrationNextAggregateStep(
  integration: Integration,
  flowId: string,
  position: number
): Step | undefined {
  const steps = getIntegrationSubsequentSteps(integration, flowId, position);
  return getNextAggregateStep(steps || [], position);
}

/**
 * Finds the closest step of type 'Aggregate' after the provided position.
 * @param steps
 * @param position
 */
export function getNextAggregateStep(
  steps: Step[],
  position: number
): Step | undefined {
  const subsequentSteps = getSubsequentSteps(steps, position);
  if (subsequentSteps && subsequentSteps.length) {
    return subsequentSteps.filter(s => s.stepKind === AGGREGATE)[0];
  }
  return undefined;
}

/**
 * Returns if the given indice is at the start of the flow
 * @param integration
 * @param flowId
 * @param position
 */
export function isStartStep(
  integration: Integration,
  flowId: string,
  position: number
) {
  return position === 0;
}

/**
 * Returns if the given indice is at the end of the flow
 * @param integration
 * @param flowId
 * @param position
 */
export function isEndStep(
  integration: Integration,
  flowId: string,
  position: number
) {
  const steps = getSteps(integration, flowId);
  return atEnd(steps, position);
}

/**
 * Returns if the given indice is at the end of the step array
 * @param steps
 * @param position
 */
export function atEnd(steps: Step[], position: number) {
  return position + 1 >= steps.length;
}

/**
 * Returns if the given indice is somewhere in the middle of the flow
 * @param integration
 * @param flowId
 * @param position
 */
export function isMiddleStep(
  integration: Integration,
  flowId: string,
  position: number
): boolean {
  return (
    !isStartStep(integration, flowId, position) &&
    !isEndStep(integration, flowId, position)
  );
}

/**
 * Creates the start connection for a conditional flow
 * @param flowId
 * @param connection
 * @param thisStep
 */
export function createConditionalFlowStart(
  flowId: string,
  connection: Connection,
  thisStep: StepKind
): StepKind {
  const step = {
    ...createStepWithConnection(connection),
    action: getConnectorAction('io.syndesis:flow-start', connection),
    configuredProperties: {
      name: flowId,
    },
    description: '',
    metadata: {
      configured: 'true',
    },
    name: 'Flow start',
    properties: {},
  } as StepKind;
  return step;
}

/**
 * Creates the end connection for a conditional flow
 * @param connection
 */
export function createConditionalFlowEnd(connection: Connection): StepKind {
  return {
    ...createStepWithConnection(connection),
    action: getConnectorAction('io.syndesis:flow-end', connection),
    description: '',
    metadata: {
      configured: 'true',
    },
    name: 'Flow end',
    properties: {},
  } as StepKind;
}

/**
 * Accessor helper function to get the given action out of a connection's connector
 * @param id
 * @param connection
 */
function getConnectorAction(id: string, connection: Connection): Action {
  return connection!.connector!.actions!.find(
    action => action.id === id
  ) as Action;
}

/**
 * Helper function to deal with the metdata map for a given thing that can
 * have a metadata map or not.
 * @param mapKey
 * @param metadata
 * @param defaultValue - value to return if the key isn't set in the map
 */
export function getMetadataValue<T>(
  mapKey: string,
  metadata?: StringMap<T>,
  defaultValue?: T
) {
  return typeof metadata !== 'undefined' ? metadata[mapKey] : defaultValue;
}

export function isIntegrationEmpty(integration: IntegrationOverview) {
  return (
    integration.flows!.length === 1 && integration.flows![0].steps!.length === 0
  );
}

/**
 * Returns true if the given integration is an API provider integration
 * @param integration
 */
export function isIntegrationApiProvider(integration: IntegrationOverview) {
  return (integration.tags || []).includes(API_PROVIDER);
}

/**
 * Returns true if the given flow is the primary flow for an integration
 * @param flow
 */
export function isPrimaryFlow(flow: Flow) {
  return (
    typeof flow !== 'undefined' &&
    (typeof flow.type === 'undefined' ||
      flow.type === FlowType.PRIMARY ||
      flow.type === FlowType.API_PROVIDER)
  );
}

/**
 * Returns true if the given flow is an alternate flow, created by the conditional flow step
 * @param flow
 */
export function isAlternateFlow(flow: Flow) {
  if (typeof flow.type !== 'undefined') {
    return flow.type === FlowType.ALTERNATE;
  }
  const step = (flow.steps || [])[0];
  try {
    return step.connection!.connectorId === FLOW;
  } catch (e) {
    // ignore
  }
  return false;
}

/**
 * Returns true if the given flow is a conditional flow created from a conditional flow step
 * @param flow
 */
export function isConditionalFlow(flow: Flow) {
  return (
    isAlternateFlow(flow) &&
    getMetadataValue<string>('kind', flow.metadata) === FlowKind.CONDITIONAL
  );
}

/**
 * Returns true if the given flow is the default flow for a conditional flow step
 * @param flow
 */
export function isDefaultFlow(flow: Flow) {
  return (
    isAlternateFlow(flow) &&
    getMetadataValue<string>(FLOW_KIND_METADATA_KEY, flow.metadata) ===
      FlowKind.DEFAULT
  );
}

/**
 * Returns true if the given flow is an API provider flow
 * @param flow
 */
export function isApiProviderFlow(flow: Flow) {
  const step = (flow.steps || [])[0];
  try {
    return step.connection!.connectorId === API_PROVIDER;
  } catch (e) {
    // ignore
  }
  return false;
}

/**
 * Returns all API provider flows in the given integration
 * @param integration
 */
export function getApiProviderFlows(integration: IntegrationOverview) {
  return (integration.flows || []).filter(isApiProviderFlow);
}

/**
 * Returns all conditional flows in the given integration
 * @param integration
 */
export function getConditionalFlows(integration: IntegrationOverview) {
  return (integration.flows || []).filter(isConditionalFlow);
}

/**
 * Returns all default flows in the given integration, there should only be one however
 * @param integration
 */
export function getDefaultFlow(integration: IntegrationOverview) {
  return (integration.flows || []).filter(isDefaultFlow);
}

export function getConditionalFlowGroups(integration: IntegrationOverview) {
  // Add default flows to the very end of the list, ensures that default flows are always at the end of a group
  const conditionalFlows = [
    ...getConditionalFlows(integration),
    ...getDefaultFlow(integration),
  ];
  // potentially we have many flows that belong to different steps, so group flows by step id
  const flowGroups: Array<{ id: string; flows: Flow[] }> = [];
  conditionalFlows.forEach(flow => {
    const stepId = getMetadataValue<string>('stepId', flow.metadata);
    const flowGroup = flowGroups.find(group => group.id === stepId);
    if (flowGroup) {
      flowGroup.flows.push(flow);
    } else {
      flowGroups.push({ id: stepId!, flows: [flow] });
    }
  });
  return flowGroups;
}

export function getConditionalFlowGroupsFor(
  integration: IntegrationOverview,
  primaryId: string
) {
  const flowGroups = getConditionalFlowGroups(integration);
  return flowGroups.filter(
    group => getPrimaryFlowId(integration, group.flows[0]) === primaryId
  );
}

export function getPrimaryFlowId(integration: IntegrationOverview, flow: Flow) {
  return getMetadataValue<string>(
    'primaryFlowId',
    flow.metadata,
    integration.flows![0].id
  );
}
