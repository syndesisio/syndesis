import { INotificationType } from '@syndesis/ui';
import * as React from 'react';

export interface IUIContext {
  hideNavigation(): void;
  showNavigation(): void;
  pushNotification(msg: string, type: INotificationType): void;
}

export const UIContextDefaultValue = {} as IUIContext;

export const UIContext = React.createContext<IUIContext>(UIContextDefaultValue);
