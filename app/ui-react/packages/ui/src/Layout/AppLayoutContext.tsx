import * as React from 'react';

export interface IAppLayoutContext {
  showBreadcrumb: (breadcrumb: any) => void;
}

export const AppLayoutContextDefaultValue = {} as IAppLayoutContext;

export const AppLayoutContext = React.createContext<IAppLayoutContext>(
  AppLayoutContextDefaultValue
);
