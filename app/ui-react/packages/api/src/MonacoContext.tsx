import * as React from 'react';

export interface IMonacoContext {
  didMountEditor: (valueGetter: any, editor: any) => void;
  willMountEditor: () => void;
}

export const MonacoContextDefaultValue = {} as IMonacoContext;

export const MonacoContext = React.createContext<IMonacoContext>(
  MonacoContextDefaultValue
);
