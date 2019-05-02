import { IntegrationMonitoring } from '@syndesis/models';
import * as React from 'react';
import { IConfigFile } from './WithConfig';

export interface IAppContext {
  config: IConfigFile;
  getPodLogUrl: (
    monitoring: IntegrationMonitoring | undefined
  ) => string | undefined;
}

export const AppContextDefaultValue = {} as IAppContext;

export const AppContext = React.createContext<IAppContext>(
  AppContextDefaultValue
);
