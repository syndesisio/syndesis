import {
  Action,
  ActionDescriptor,
  ActionDescriptorStep,
  ConfigurationProperty,
  Connection,
  ConnectionBulletinBoard,
  IConnectionOverview,
  IConnectionWithIconFile,
  IConnector,
} from '@syndesis/models';
import { getMetadataValue } from './integrationFunctions';

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
  connection: IConnectionOverview
): IConnector {
  if (!connection.connector) {
    throw Error(`FATAL: Connection ${connection.id} doesn't have a connector`);
  }
  return connection.connector;
}

export function getConnectorActions(connector: IConnector): Action[] {
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
  // The svg connection's icon is in UI's assets dir
  if (connection.icon.toLowerCase().startsWith('assets:')) {
    return `./../../icons/${connection.icon.substring(7)}`;
  }
  // Legacy connections rely on the icon being in the UI's assets
  return `./../../icons/${connection.icon}.connection.png`;
}

/**
 * Checks whether the ConnectionBulletinBoard provided
 * @param board
 */
export function isConfigRequired(board: ConnectionBulletinBoard): boolean {
  return (board.notices || board.warnings || board.errors)! > 0;
}

/**
 * Checks whether or not the provided object is a technical preview.
 * Accepts a Connector.
 * Returns a boolean for whether or not the metadata `tech-preview` key
 * returns a string value of 'true'
 * @param connector
 */
export function isTechPreview(connector: IConnector): boolean {
  return (
    getMetadataValue<string>('tech-preview', connector.metadata) === 'true'
  );
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
