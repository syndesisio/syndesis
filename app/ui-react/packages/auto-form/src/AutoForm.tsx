import { Formik, FormikActions, FormikErrors, FormikProps } from 'formik';
import * as React from 'react';
import {
  AutoFormContext,
  AutoFormContextDefaultValue,
} from './AutoFormContext';
import { IAutoFormActions, IFormDefinition, IFormErrors } from './models';
import { useFormBuilder } from './useFormBuilder';

export interface IAutoFormProps<T> {
  /**
   * A map of configuration properties as returned by the Syndesis API
   */
  definition: IFormDefinition;
  /**
   * The initial value that should be set on the form
   */
  initialValue?: T;
  /**
   * If the passed in value is valid or not
   */
  isInitialValid?: boolean;
  /**
   * If all fields in the form are required or not
   */
  allFieldsRequired?: boolean;
  /**
   * Map of custom components, each key maps to the 'type'
   * property of an IFormDefinitionProperty
   */
  customComponents?: { [type: string]: any };
  /**
   * String to be displayed when a required field isn't set
   */
  i18nRequiredProperty: string;
  /**
   * String to be displayed when some or all properties are required
   */
  i18nFieldsStatusText?: string;
  /**
   * Callback function that will be called when the form is submitted
   */
  onSave?: (value: T, autoFormBag: IAutoFormActions<T>) => void;
  /**
   * Validation function called whenever a change or blur event occurs on the form
   */
  validate?: (
    value: T | any
  ) => IFormErrors<T> | Promise<IFormErrors<T>> | undefined;

  /**
   * Validation function called to determine if the initial values are valid
   */
  validateInitial?: (value: T | any) => IFormErrors<T>;
  /**
   * Child component that will receive the form fields and submit handler
   */
  children: (props: IAutoFormChildrenProps<T> & FormikProps<T>) => any;
}

export interface IAutoFormChildrenProps<T> {
  /**
   * Fragment containing all of the form fields
   */
  fields: JSX.Element;
  /**
   * The same fields as an array of separate elements
   */
  fieldsAsArray: JSX.Element[];
  /**
   * Function to trigger a form submit which will then trigger onSave
   */
  validateForm: () => Promise<IFormErrors<T> | FormikErrors<T>>;
}

export const AutoForm = <T extends any>(
  props: IAutoFormProps<T>
): React.ReactElement => {
  const { getField, getPropertiesArray, getInitialValues } = useFormBuilder();
  const initialValues = getInitialValues(
    props.definition,
    props.initialValue
  ) as T;
  const propertiesArray = getPropertiesArray(props.definition);
  const isInitialValid =
    typeof props.validateInitial === 'function'
      ? Object.keys(props.validateInitial(initialValues) || {}).length === 0
      : props.isInitialValid || false;

  const handleSave = (value: T, formikBag: FormikActions<T>) => {
    if (typeof props.onSave === 'function') {
      props.onSave(value, formikBag as IAutoFormActions<T>);
    }
  };

  return (
    <AutoFormContext.Provider
      value={{
        typemaps: {
          ...AutoFormContextDefaultValue.typemaps,
          ...props.customComponents,
        },
      }}
    >
      <Formik<T>
        initialValues={initialValues}
        onSubmit={handleSave}
        isInitialValid={isInitialValid}
        validate={props.validate}
      >
        {({ values, touched, dirty, errors, ...rest }) => {
          const propertyComponents = propertiesArray.map(property => {
            const err =
              typeof errors === 'object' ? errors : { [property.name]: errors };
            return getField({
              allFieldsRequired: props.allFieldsRequired || false,
              errors: err as IFormErrors<T>,
              property,
              value: (values || {})[property.name],
              ...rest,
            });
          });
          return props.children({
            dirty,
            errors,
            fields: (
              <React.Fragment>
                {props.i18nFieldsStatusText && (
                  <p
                    className="fields-status-pf"
                    dangerouslySetInnerHTML={{
                      __html: props.i18nFieldsStatusText,
                    }}
                  />
                )}
                {propertyComponents}
              </React.Fragment>
            ),
            fieldsAsArray: propertyComponents,
            values,
            ...(rest as FormikProps<T>),
          });
        }}
      </Formik>
    </AutoFormContext.Provider>
  );
};
