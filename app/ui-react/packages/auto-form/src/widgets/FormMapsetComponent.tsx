import * as React from 'react';
import { IFormControlProps } from '../models';

export const FormMapsetComponent: React.FunctionComponent<
  IFormControlProps
> = props => (
  <div {...props.property.formGroupAttributes}>
    <>
      <h2>This is TODO</h2>
      <pre>{JSON.stringify(props.field.value, undefined, 2)}</pre>
    </>
  </div>
);
