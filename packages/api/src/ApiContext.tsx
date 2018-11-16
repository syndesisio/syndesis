import * as React from 'react';

export interface IApiContext {
  apiUri: string;
  token?: string;
}

export const ApiContextDefaultValue = {
  apiUri: 'http://example.com',
} as IApiContext;

export const ApiContext = React.createContext<IApiContext>(
  ApiContextDefaultValue
);
