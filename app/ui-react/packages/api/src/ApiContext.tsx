import * as React from 'react';

export interface IApiContext {
  apiUri: string;
  dvApiUri: string;
  headers: { [name: string]: string };
}

export const ApiContextDefaultValue = {
  apiUri: 'http://example.com',
  dvApiUri: 'http://example.com',
  headers: {},
} as IApiContext;

export const ApiContext = React.createContext<IApiContext>(
  ApiContextDefaultValue
);
