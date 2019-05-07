import {
  Extension,
  IConnectionWithIconFile,
  Integration,
  IntegrationOverview,
  Step,
} from '@syndesis/models';
import { key } from '@syndesis/utils';
import produce from 'immer';
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
  return getIntegrationStepIcon(apiUri, integration, flow.id!, 0);
}

/**
 * Returns the ending icon representing the provided integration
 * @param integration
 */
export function getFinishIcon(apiUri: string, integration: Integration) {
  const flow = integration.flows![0];
  return getIntegrationStepIcon(
    apiUri,
    integration,
    flow.id!,
    flow.steps!.length - 1
  );
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
export function getIntegrationStepIcon(
  apiUri: string,
  integration: Integration,
  flowId: string,
  stepIndex: number
): string {
  const step = getStep(integration, flowId, stepIndex);
  return getStepIcon(apiUri, step);
}

/**
 * Returns the icon for the supplied step
 * @param apiUri
 * @param step
 */
export function getStepIcon(apiUri: string, step: Step): string {
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
  return getStepsLastPosition(flow.steps);
}
export function getStepsLastPosition(steps: Step[]) {
  if (steps.length <= 1) {
    return 1;
  }
  return steps.length - 1;
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
