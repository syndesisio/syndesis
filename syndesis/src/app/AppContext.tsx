import * as React from 'react';
import { IConfigFile } from './config';

export interface IAppContext {
  config: IConfigFile;
  logout(): void;
}

export const AppContextDefaultValue = {} as IAppContext;

export const AppContext = React.createContext<IAppContext>(
  AppContextDefaultValue
);
