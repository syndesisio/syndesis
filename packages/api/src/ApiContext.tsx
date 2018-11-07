import * as React from 'react';

export interface IApiContext {
  apiUri: string;
  token: string | null;
}

export const ApiContextDefaultValue = {
  apiUri: 'http://example.com',
  token: null
} as IApiContext;

export const ApiContext = React.createContext<IApiContext>(
  ApiContextDefaultValue
);
