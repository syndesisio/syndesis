import * as React from 'react';

export const FormTextAreaComponent = ({
  field,
  form: { touched, errors, isSubmitting },
  ...props
}: {
  [name: string]: any;
}) => (
  // TODO replace with PF3/PF4 widget
  <div className="form-group">
    <label className="control-label" htmlFor={field.name}>
      {props.property.displayName}
    </label>
    <textarea
      id={field.name}
      data-testid={field.name}
      disabled={isSubmitting}
      className={'form-control'}
      {...field}
    />
    {touched[field.name] && errors[field.name] && (
      <div className="error">{errors[field.name]}</div>
    )}
  </div>
);
