import * as React from 'react';
import { IFormControl } from '../models';

export const FormHiddenComponent: React.FunctionComponent<
  IFormControl
> = props => (
  // TODO replace with PF3/PF4 widget
  <div>
    <input
      type={props.type}
      id={props.name}
      data-testid={props.name}
      {...props.field}
    />
    {props.form.touched[props.field.name] &&
      props.form.errors[props.field.name] && (
        <div className="error">{props.form.errors[props.field.name]}</div>
      )}
  </div>
);
