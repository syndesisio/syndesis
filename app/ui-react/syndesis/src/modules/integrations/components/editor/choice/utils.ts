import { createStepWithConnection } from '@syndesis/api';
import { Action, Connection, Flow, StepKind } from '@syndesis/models';
import { key } from '@syndesis/utils';
import {
  IChoiceConfiguration,
  ICreateFlowProps,
  IFlowOption,
} from './interfaces';

export enum FlowType {
  PRIMARY = 'PRIMARY',
  ALTERNATE = 'ALTERNATE',
}

/**
 * Creates a new flow object with flow start/end connections for conditional flows
 * @param props
 */
export function createFlow(props: ICreateFlowProps) {
  const flowId = key();
  return {
    connections: [],
    description: props.description,
    id: flowId,
    metadata: {
      excerpt: '',
      kind: props.kind,
      primaryFlowId: props.primaryFlowId,
      stepId: props.step.id,
    },
    name: props.name,
    steps: [
      createFlowStart(flowId, props.flowConnectionTemplate, props.step),
      createFlowEnd(props.flowConnectionTemplate),
    ],
    type: FlowType.ALTERNATE,
  } as Flow;
}

/**
 * Builds a sane choice configuration object from the step's configured properties
 * @param configuredProperties
 */
export function createChoiceConfiguration(configuredProperties: {
  [key: string]: any;
}) {
  const flows = (typeof configuredProperties.flows === 'string'
    ? JSON.parse(configuredProperties.flows)
    : configuredProperties.flows || []) as IFlowOption[];
  const defaultFlowEnabled = typeof configuredProperties.default === 'string';
  const defaultFlow = configuredProperties.default;
  const routingScheme = configuredProperties.routingScheme || 'direct';
  return {
    defaultFlow,
    defaultFlowEnabled,
    flows,
    routingScheme,
  } as IChoiceConfiguration;
}

/**
 * Creates the start connection for a conditional flow
 * @param flowId
 * @param connection
 * @param thisStep
 */
export function createFlowStart(
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
  adaptOutputShape(thisStep, step);
  return step;
}

/**
 * Creates the end connection for a conditional flow
 * @param connection
 */
export function createFlowEnd(connection: Connection): StepKind {
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

function getConnectorAction(id: string, connection: Connection): Action {
  return connection!.connector!.actions!.find(
    action => action.id === id
  ) as Action;
}

function adaptOutputShape(thisStep: StepKind, step: StepKind) {
  if (
    thisStep &&
    thisStep.action &&
    thisStep.action.descriptor &&
    thisStep.action.descriptor.inputDataShape
  ) {
    step.action!.descriptor!.outputDataShape =
      thisStep.action.descriptor.inputDataShape;
  }
}
