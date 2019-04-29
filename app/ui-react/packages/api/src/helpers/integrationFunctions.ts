import {
  Extension,
  IConnectionWithIconFile,
  Integration,
  IntegrationOverview,
  Step,
} from '@syndesis/models';
import { Connection, ConnectorAction, StepKind } from '@syndesis/models/src';
import { key } from '@syndesis/utils';
import produce from 'immer';
import {
  ADVANCED_FILTER,
  AGGREGATE,
  BASIC_FILTER,
  DATA_MAPPER,
  ENDPOINT,
  HIDE_FROM_STEP_SELECT,
  SPLIT,
  TEMPLATE,
} from '../constants';
import { getConnectionIcon } from './connectionFunctions';

export const NEW_INTEGRATION = {
  name: '',
  tags: [],
} as Integration;

/**
 * returns an empty integration object.
 *
 * @todo make the returned object immutable to avoid uncontrolled changes
 */
export function getEmptyIntegration(): Integration {
  return produce(NEW_INTEGRATION, draft => {
    draft.flows = [
      {
        id: key(),
        name: '',
        steps: [],
      },
    ];
  });
}

/**
 * updates the name of an integration.
 *
 * @param integration
 * @param name
 */

export function setIntegrationName(
  integration: Integration,
  name: string
): Integration {
  return produce(integration, nextIntegration => {
    nextIntegration.name = name;
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

/**
 * returns a specific step of the provided integration.
 *
 * @param integration
 * @param flowId
 * @param step
 *
 * @todo make the returned object immutable to avoid uncontrolled changes
 */

export function getStep(
  integration: Integration,
  flowId: string,
  step: number
): Step {
  try {
    const flow = getFlow(integration, flowId);
    return flow!.steps![step];
  } catch (e) {
    throw new Error(
      `Can't find a step ${step} for flow ${flowId} in integration ${
        integration.id
      }`
    );
  }
}

/**
 * Returns the start icon representing the provided integration
 * @param integration
 */
export function getStartIcon(apiUri: string, integration: Integration) {
  const flow = integration.flows![0];
  return getStepIcon(apiUri, integration, flow.id!, 0);
}

/**
 * Returns the ending icon representing the provided integration
 * @param integration
 */
export function getFinishIcon(apiUri: string, integration: Integration) {
  const flow = integration.flows![0];
  return getStepIcon(apiUri, integration, flow.id!, flow.steps!.length - 1);
}

export function getExtensionIcon(extension: Extension) {
  return extension.icon || ''; // TODO: a default icon?
}

export function getStepKindIcon(stepKind: Step['stepKind']) {
  return `/icons/steps/${stepKind}.svg`;
}

/**
 * Returns the icon for the supplied step index of the supplied flow index
 * @param apiUri
 * @param integration
 * @param flowId
 * @param stepIndex
 */
export function getStepIcon(
  apiUri: string,
  integration: Integration,
  flowId: string,
  stepIndex: number
): string {
  const step = getStep(integration, flowId, stepIndex);
  // The step is a connection
  if (step.connection) {
    const connection = step.connection as IConnectionWithIconFile;
    return getConnectionIcon(apiUri, connection);
  }
  // The step is an extension
  if (step.extension) {
    return getExtensionIcon(step.extension);
  }
  // It's just a step
  return getStepKindIcon(step.stepKind);
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
 * Returns the last index of the step array of the given flow or undefined if it hasn't been created
 * @param integration
 * @param flowId
 */
export function getLastPosition(integration: Integration, flowId: string) {
  if (!flowId) {
    return undefined;
  }
  const flow = getFlow(integration, flowId);
  if (!flow) {
    return undefined;
  }
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
 * Filters connections based on the supplied position in the step array
 * @param steps
 * @param position
 */
export function filterStepsByPosition(
  integration: Integration,
  flowId: string,
  steps: StepKind[],
  position: number
) {
  if (typeof position === 'undefined' || !steps) {
    // safety net
    return steps;
  }
  const atStart = position === 0;
  const atEnd = getLastPosition(integration, flowId) === position;
  return steps.filter(step => {
    // Hide steps that are marked as such, and specifically the log connection
    if (
      (typeof step.connection !== 'undefined' &&
        typeof step.connection.metadata !== 'undefined' &&
        step.connection.metadata[HIDE_FROM_STEP_SELECT]) ||
      (typeof step.metadata !== 'undefined' &&
        step.metadata[HIDE_FROM_STEP_SELECT]) ||
      (step as Connection).connectorId === 'log'
    ) {
      return false;
    }
    // Special handling for the beginning of a flow
    if (atStart) {
      // At the moment only endpoints can be at the start
      if (step.stepKind) {
        return false;
      }
      if ((step as Connection).connector) {
        return (step as Connection).connector!.actions.some(
          (action: ConnectorAction) => {
            return action.pattern === 'From';
          }
        );
      }
      // it's not a connection
      return true;
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
    if (step.stepKind !== ENDPOINT) {
      return true;
    }
    // Only show connections that have at least one action that accepts data
    if ((step as Connection).connector) {
      return (step as Connection).connector!.actions.some(
        (action: ConnectorAction) => {
          return action.pattern === 'To';
        }
      );
    }
    return true;
  });
}

/**
 * Filters connections based on the supplied position in the step array and their
 * visibility status
 * @param steps
 * @param position
 */
export function visibleStepsByPosition(
  integration: Integration,
  flowId: string,
  steps: StepKind[],
  position: number
) {
  const previousSteps = getPreviousSteps(integration, flowId, position);
  const subsequentSteps = getSubsequentSteps(integration, flowId, position);
  return filterStepsByPosition(integration, flowId, steps, position).filter(s =>
    s.visible
      ? typeof s.visible === 'function'
        ? s.visible(position, previousSteps, subsequentSteps)
        : s.visible
      : true
  );
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
  if (!flow || !flow.steps) {
    // TODO following semantics for now, this should throw an error
    return [];
  }
  return flow.steps.slice(0, position);
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
  if (!flow || !flow.steps) {
    // TODO following semantics for now, this should throw an error
    return [];
  }
  return flow.steps.slice(position + 1);
}
