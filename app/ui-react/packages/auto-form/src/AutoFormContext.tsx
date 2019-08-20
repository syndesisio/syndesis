import * as React from 'react';
import {
  FormCheckboxComponent,
  FormDurationComponent,
  FormHiddenComponent,
  FormLegendComponent,
  FormMapsetComponent,
  FormSelectComponent,
  FormTextAreaComponent,
} from './widgets';

export interface IComponentTypemap {
  [name: string]: object;
}

export interface IAutoFormContext {
  typemaps: IComponentTypemap;
}

export const AutoFormContextDefaultValue = {
  typemaps: {
    checkbox: FormCheckboxComponent,
    duration: FormDurationComponent,
    hidden: FormHiddenComponent,
    legend: FormLegendComponent,
    mapset: FormMapsetComponent,
    select: FormSelectComponent,
    textarea: FormTextAreaComponent,
  },
} as IAutoFormContext;

export const AutoFormContext = React.createContext<IAutoFormContext>(
  AutoFormContextDefaultValue
);
