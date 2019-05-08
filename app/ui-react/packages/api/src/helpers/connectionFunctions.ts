import {
  Action,
  ActionDescriptor,
  ActionDescriptorStep,
  ConfigurationProperty,
  Connection,
  ConnectionOverview,
  Connector,
  IConnectionWithIconFile,
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

export function getConnectionIcon(
  apiUri: string,
  connection: IConnectionWithIconFile
) {
  if (
    typeof connection.icon === 'undefined' &&
    typeof connection.iconFile === 'undefined'
  ) {
    // The connection has no icon for whatever reason
    // TODO: sensible default icon
    return '';
  }
  // Connections created from the API client connector can have a custom icon file
  if (connection.iconFile || connection.icon instanceof File) {
    const file = connection.iconFile || connection.icon;
    const tempIconBlobPath = URL.createObjectURL(file);
    return tempIconBlobPath;
  }
  // The connection has an embedded icon
  if (connection.icon.toLowerCase().startsWith('data:')) {
    return connection.icon;
  }
  // The connection's icon is stored in the DB in some weird way
  if (
    connection.icon.toLowerCase().startsWith('db:') ||
    connection.icon.toLowerCase().startsWith('extension:')
  ) {
    return `${apiUri}/connectors/${connection.id}/icon?${connection.icon}`;
  }
  // Legacy connections rely on the icon being in the UI's assets
  return `./../../icons/${connection.icon}.connection.png`;
}
