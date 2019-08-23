import { Field, FieldArray } from 'formik';
import * as React from 'react';
import { AutoFormContext } from './AutoFormContext';
import {
  IFormArrayControlProps,
  IFormControlProps,
  IFormDefinition,
  IRenderFieldProps,
} from './models';
import { enrichAndOrderProperties, massageType, sanitizeValues } from './utils';
import { FormArrayComponent, FormInputComponent } from './widgets';

export function useFormBuilder() {
  const autoFormContext = React.useContext(AutoFormContext);
  /**
   * Converts a property configuration to some kind of input field
   * @param props
   */
  const getField = (props: IRenderFieldProps): any => {
    // Massage the value in the definition to an input type
    const type = massageType(props.property);
    const componentTypemaps = autoFormContext.typemaps;
    switch (type) {
      case 'array':
        return (
          <FieldArray
            {...props as IFormArrayControlProps}
            key={props.property.name}
            name={props.property.name}
            render={helpers => <FormArrayComponent {...props} {...helpers} />}
          />
        );
      default:
        return (
          <Field
            key={props.property.name}
            name={props.property.name}
            type={type}
            {...props as IFormControlProps}
            component={componentTypemaps[type] || FormInputComponent}
          />
        );
    }
  };

  const getPropertiesArray = (definition: IFormDefinition) => {
    return enrichAndOrderProperties(definition);
  };

  const getInitialValues = (
    definition: IFormDefinition,
    initialValues: any
  ) => {
    return sanitizeValues(definition, initialValues);
  };

  return {
    getField,
    getInitialValues,
    getPropertiesArray,
  };
}
