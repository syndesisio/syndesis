import * as React from 'react';

export interface IAppSettings {
  apiUri: string;
  authorizationUri: string;
}

export interface IAppContext extends IAppSettings {
  firstSetup: boolean;

  saveSettings(settings: IAppSettings): void;

  logout(): void;
}

export const AppContextDefaultValue = {
  apiUri: 'http://example.com',
  authorizationUri: 'http://example.com/oauth/authorize',
  firstSetup: true
} as IAppContext;

export const AppContext = React.createContext<IAppContext>(
  AppContextDefaultValue
);
