import * as React from 'react';
import { IFormControl } from '../models';

export const FormHiddenComponent: React.FunctionComponent<
  IFormControl
> = props => (
  <div>
    <input
      {...props.property.fieldAttributes}
      {...props.field}
      type={props.type}
      id={props.name}
      data-testid={props.name}
    />
    {props.form.touched[props.field.name] &&
      props.form.errors[props.field.name] && (
        <div className="error">{props.form.errors[props.field.name]}</div>
      )}
  </div>
);
