import * as React from 'react';

export interface IConnectionsAppContext {
  baseurl: string;
}

export const ConnectionsAppContextDefaultValue = {
  baseurl: ''
} as IConnectionsAppContext;

export const ConnectionsAppContext = React.createContext<IConnectionsAppContext>(
  ConnectionsAppContextDefaultValue
);
