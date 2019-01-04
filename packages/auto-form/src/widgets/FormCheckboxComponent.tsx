import * as React from 'react';

export const FormCheckboxComponent = ({
  field,
  type,
  form: { touched, errors },
  ...props
}: {
  [name: string]: any;
}) => (
  // TODO replace with PF3/PF4 widget
  <div className="form-group">
    <label className="col-sm-2 control-label" htmlFor={field.name}>
      {props.property.displayName}
    </label>
    <div className="col-sm-10">
      <input
        type={type}
        id={field.name}
        data-testid={field.name}
        {...field}
        checked={field.value === 'true'}
      />
      {touched[field.name] && errors[field.name] && (
        <div className="error">{errors[field.name]}</div>
      )}
    </div>
  </div>
);
