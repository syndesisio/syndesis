import {
  Action,
  ActionDescriptor,
  ActionDescriptorStep,
  ConfigurationProperty,
  Connection,
  ConnectionOverview,
  Connector,
} from '@syndesis/models';

export function getActionsWithFrom(actions: Action[] = []) {
  return actions.filter(a => a.pattern === 'From');
}

export function getActionsWithTo(actions: Action[] = []) {
  return actions.filter(a => a.pattern === 'To');
}

export function getConnectionMetadataValue(
  connection: Connection,
  key: string
) {
  const metadata = connection.metadata || {};
  return metadata[key];
}

export function getConnectionConnector(
  connection: ConnectionOverview
): Connector {
  if (!connection.connector) {
    throw Error(`FATAL: Connection ${connection.id} doesn't have a connector`);
  }
  return connection.connector;
}

export function getConnectorActions(connector: Connector): Action[] {
  return connector.actions;
}

export function getActionById(actions: Action[], actionId: string): Action {
  const action = actions.find(a => a.id === actionId);
  if (!action) {
    throw Error(`FATAL: Action ${actionId} not found`);
  }
  return action;
}

export function getActionDescriptor(action: Action): ActionDescriptor {
  if (!action.descriptor) {
    throw Error(`FATAL: Action ${action.id} doesn't have any descriptor`);
  }
  return action.descriptor;
}

export function getActionSteps(
  descriptor: ActionDescriptor
): ActionDescriptorStep[] {
  if (!descriptor.propertyDefinitionSteps) {
    throw Error(`FATAL: Descriptor doesn't have any definition`);
  }
  return descriptor.propertyDefinitionSteps;
}

export function getActionStep(
  steps: ActionDescriptorStep[],
  step: number
): ActionDescriptorStep {
  if (step > steps.length - 1) {
    throw Error(`FATAL: Step ${step} does not exist in the descriptor steps`);
  }
  return steps[step];
}

export function getActionStepDefinition(
  step: ActionDescriptorStep
): {
  [name: string]: ConfigurationProperty;
} {
  if (!step.properties) {
    throw Error(`FATAL: Step ${step} does not have valid properties`);
  }
  return step.properties;
}

/**
 * Return bool if a connection is derived, meaning that its configuration comes
 * from an OAuth flow. This helper is to work around a bug in the swagger definition
 * that names the property we can use to know this as `derived`, but it's really
 * returned to us as `isDerived`.
 *
 * To make the helper future proof, this uses both properties to save us from
 * a sudden fix in the API.
 * @param connection
 */
export function isDerived(connection: Connection) {
  return (connection as any).isDerived || connection.derived;
}
