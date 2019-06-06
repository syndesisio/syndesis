import { ApiContext, ENDPOINT, EXTENSION } from '@syndesis/api';
import {
  Connection,
  Extension,
  IConnectionWithIconFile,
  Step,
} from '@syndesis/models';
import * as React from 'react';

const UNKNOWN_ICON = '/icons/unknown.svg';

/**
 * Returns the icon URL for a connection object and things like it
 * @param apiUri
 * @param connection
 */
function getConnectionIcon(
  apiUri: string,
  connection: IConnectionWithIconFile
) {
  if (
    typeof connection.icon === 'undefined' &&
    typeof connection.iconFile === 'undefined'
  ) {
    return UNKNOWN_ICON;
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
    return `${apiUri}/connectors/${connection.connectorId}/icon?${
      connection.icon
    }`;
  }
  if (connection.icon.toLowerCase().startsWith('assets:')) {
    const fileName = connection.icon.replace('assets:', '');
    return `/icons/${fileName}`;
  }
  // Legacy connections rely on the icon being in the UI's assets
  return `/icons/${connection.icon}.connection.png`;
}

/**
 * Returns the icon URL for an extension object
 * @param apiUri
 * @param extension
 */
function getExtensionIcon(apiUri: string, extension: Extension) {
  // The connection's icon is stored in the DB in some weird way
  if (
    extension.icon &&
    (extension.icon.toLowerCase().startsWith('db:') ||
      extension.icon.toLowerCase().startsWith('extension:'))
  ) {
    // unfortunately we have to reverse engineer the connector ID
    const connectorId = 'ext-' + extension.extensionId!.replace(/\.|:/g, '-');
    return `${apiUri}/connectors/${connectorId}/icon?${extension.icon}`;
  }
  return extension.icon || UNKNOWN_ICON;
}

/**
 * Returns the icon for one of the UI's built in step
 * @param stepKind
 */
function getStepKindIcon(stepKind: Step['stepKind']) {
  return `/icons/steps/${stepKind}.svg`;
}

/**
 * Returns the icon for the supplied step
 * @param apiUri
 * @param step
 */
export function getStepIcon(
  apiUri: string,
  step: Step | Connection | Extension
): string {
  const extensionType = (step as Extension).extensionId;
  if (typeof extensionType === 'string') {
    return getExtensionIcon(apiUri, step as Extension);
  }
  const kind = (step as Step).stepKind;
  if (typeof kind !== 'undefined') {
    const stepKind = step as Step;
    if (kind === ENDPOINT) {
      return getConnectionIcon(apiUri, stepKind.connection!);
    }
    // The step is an extension
    if ((step as Step).stepKind === EXTENSION) {
      return getExtensionIcon(apiUri, stepKind.extension!);
    }
    // It's just a step
    return getStepKindIcon(kind);
  }
  return getConnectionIcon(apiUri, step as Connection);
}

export interface IEntityIconProps {
  entity: any;
  alt: string;
  width?: number;
  height?: number;
  className?: string;
}

export const EntityIcon: React.FunctionComponent<IEntityIconProps> = ({
  alt,
  className,
  entity,
  height,
  width,
}) => (
  <>
    <ApiContext.Consumer>
      {({ apiUri }) => {
        const icon = getStepIcon(apiUri, entity);
        return (
          <img
            className={className}
            src={icon}
            alt={alt}
            width={width}
            height={height}
          />
        );
      }}
    </ApiContext.Consumer>
  </>
);
