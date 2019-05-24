import * as React from 'react';
import { IChangeEvent, IMessageEvent } from './WithServerEvents';

export interface IServerEventsContext {
  registerChangeListener: (listener: (event: IChangeEvent) => void) => void;
  registerMessageListener: (listener: (event: IMessageEvent) => void) => void;
  unregisterChangeListener: (listener: (event: IChangeEvent) => void) => void;
  unregisterMessageListener: (listener: (event: IMessageEvent) => void) => void;
}

export const ServerEventsContextDefaultValue = {} as IServerEventsContext;

export const ServerEventsContext = React.createContext<IServerEventsContext>(
  ServerEventsContextDefaultValue
);
