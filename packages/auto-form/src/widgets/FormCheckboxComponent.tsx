import * as React from 'react';

export const FormCheckboxComponent = ({
  field,
  type,
  form: { touched, errors, isSubmitting },
  ...props
}: {
  [name: string]: any;
}) => (
  // TODO replace with PF3/PF4 widget
  <div className="form-group">
    <div className="checkbox">
      <label htmlFor={field.name}>
        <input
          type={type}
          id={field.name}
          data-testid={field.name}
          {...field}
          checked={field.value === 'true'}
          disabled={isSubmitting}
        />
        {props.property.displayName}
      </label>
      {touched[field.name] && errors[field.name] && (
        <div className="error">{errors[field.name]}</div>
      )}
    </div>
  </div>
);
