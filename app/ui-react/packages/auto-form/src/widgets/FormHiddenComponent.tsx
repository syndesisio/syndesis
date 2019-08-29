import * as React from 'react';
import { IFormControlProps } from '../models';
import { toValidHtmlId } from './helpers';

export const FormHiddenComponent: React.FunctionComponent<
  IFormControlProps
> = props => (
  <div {...props.property.formGroupAttributes} style={{ display: 'none' }}>
    <input
      {...props.property.fieldAttributes}
      {...props.field}
      type={props.type}
      id={toValidHtmlId(props.field.name)}
      data-testid={toValidHtmlId(props.field.name)}
    />
    {props.form.touched[props.field.name] &&
      props.form.errors[props.field.name] && (
        <div className="error">{props.form.errors[props.field.name]}</div>
      )}
  </div>
);
