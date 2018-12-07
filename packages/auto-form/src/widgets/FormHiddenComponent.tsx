import * as React from 'react';

export const FormHiddenComponent = ({
  field,
  type,
  form: { touched, errors },
  ...props
}: {
  [name: string]: any;
}) => (
  // TODO replace with PF3/PF4 widget
  <div>
    <input type={type} id={field.name} data-testid={field.name} {...field} />
    {touched[field.name] && errors[field.name] && (
      <div className="error">{errors[field.name]}</div>
    )}
  </div>
);
