import { IntegrationMonitoring } from '@syndesis/models';
import { INotificationType } from '@syndesis/ui';
import * as React from 'react';
import { IConfigFile } from './WithConfig';

export interface IAppContext {
  config: IConfigFile;
  getPodLogUrl: (
    config: IConfigFile,
    monitoring: IntegrationMonitoring | undefined
  ) => string | undefined;
  logout(): void;
  hideNavigation(): void;
  showNavigation(): void;
  pushNotification(msg: string, type: INotificationType): void;
}

export const AppContextDefaultValue = {} as IAppContext;

export const AppContext = React.createContext<IAppContext>(
  AppContextDefaultValue
);
