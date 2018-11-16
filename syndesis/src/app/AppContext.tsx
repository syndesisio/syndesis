import * as React from 'react';
import { IConfigFile } from './WithConfig';

export interface IAppContext {
  config: IConfigFile;
  logout(): void;
}

export const AppContextDefaultValue = {} as IAppContext;

export const AppContext = React.createContext<IAppContext>(
  AppContextDefaultValue
);
