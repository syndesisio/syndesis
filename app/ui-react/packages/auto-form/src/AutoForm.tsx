import { Formik, FormikActions, FormikErrors, FormikProps } from 'formik';
import * as React from 'react';
import { FormBuilder } from './FormBuilder';
import { IAutoFormActions, IFormDefinition, IFormErrors } from './models';

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
   * Child component that will receive the form fields and submit handler
   */
  children: (props: IAutoFormChildrenProps<T> & FormikProps<T>) => any;
}

export interface IAutoFormChildrenProps<T> {
  /**
   * Fragment containing all of the form fields
   */
  fields: JSX.Element;
  fieldsAsArray: JSX.Element[];
  /**
   * Function to trigger a form submit which will then trigger onSave
   */
  validateForm: () => Promise<IFormErrors<T> | FormikErrors<T>>;
}

export class AutoForm<T> extends React.Component<IAutoFormProps<T>> {
  constructor(props: IAutoFormProps<T>) {
    super(props);
  }
  public handleSave(value: T, formikBag: FormikActions<T>) {
    if (typeof this.props.onSave === 'function') {
      this.props.onSave(value, formikBag as IAutoFormActions<T>);
    }
  }
  public render() {
    return (
      <FormBuilder
        definition={this.props.definition}
        initialValue={this.props.initialValue || ({} as T)}
        customComponents={this.props.customComponents || {}}
        i18nRequiredProperty={this.props.i18nRequiredProperty}
      >
        {({ initialValue, propertiesArray, getField }) => (
          <Formik<T>
            initialValues={initialValue}
            onSubmit={
              this.props.onSave ||
              (() => {
                /* todo right now silently ignore */
              })
            }
            isInitialValid={this.props.isInitialValid}
            validate={this.props.validate}
          >
            {({ values, touched, dirty, errors, ...rest }) => {
              const propertyComponents = propertiesArray.map(property => {
                const err =
                  typeof errors === 'object'
                    ? errors
                    : { [property.name]: errors };
                return getField({
                  allFieldsRequired: this.props.allFieldsRequired || false,
                  errors: err as IFormErrors<T>,
                  property,
                  value: (values || {})[property.name],
                  ...rest,
                });
              });
              return this.props.children({
                dirty,
                errors,
                fields: (
                  <React.Fragment>
                    {this.props.i18nFieldsStatusText && (
                      <p
                        className="fields-status-pf"
                        dangerouslySetInnerHTML={{
                          __html: this.props.i18nFieldsStatusText,
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
        )}
      </FormBuilder>
    );
  }
}
