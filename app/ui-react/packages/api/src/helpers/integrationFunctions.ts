import {
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
 * @param value
 * @param flow
 *
 * @todo make the returned object immutable to avoid uncontrolled changes
 */
export function getSteps(integration: Integration, flow: number): Step[] {
  try {
    return integration.flows![flow].steps!;
  } catch (e) {
    throw new Error(`Can't find steps in position flow:${flow}`);
  }
}

/**
 * returns a specific step of the provided integration.
 *
 * @param value
 * @param flow
 *
 * @todo make the returned object immutable to avoid uncontrolled changes
 */

export function getStep(
  integration: Integration,
  flow: number,
  step: number
): Step {
  try {
    return integration.flows![flow].steps![step];
  } catch (e) {
    throw new Error(`Can't find a step in position flow:${flow} step:${step}`);
  }
}

/**
 * Returns the start icon representing the provided integration
 * @param integration
 */
export function getStartIcon(apiUri: string, integration: Integration) {
  return getStepIcon(apiUri, integration, 0, 0);
}

/**
 * Returns the ending icon representing the provided integration
 * @param integration
 */
export function getFinishIcon(apiUri: string, integration: Integration) {
  const flow = integration.flows![0];
  return getStepIcon(apiUri, integration, 0, flow.steps!.length - 1);
}

/**
 * Returns the icon for the supplied step index of the supplied flow index
 * @param integration
 * @param flowIndex
 * @param stepIndex
 */
export function getStepIcon(
  apiUri: string,
  integration: Integration,
  flowIndex: number,
  stepIndex: number
): string {
  const step = getStep(integration, flowIndex, stepIndex);
  // The step is a connection
  if (step.connection) {
    const connection = step.connection as IConnectionWithIconFile;
    return getConnectionIcon(apiUri, connection);
  }
  // The step is an extension
  if (step.extension && step.extension.icon) {
    return step.extension.icon;
  }
  // It's just a step
  return `./../../icons/steps/${step.stepKind}.svg`;
}
