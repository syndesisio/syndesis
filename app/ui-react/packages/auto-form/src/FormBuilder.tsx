import { Field, FieldArray } from 'formik';
import * as React from 'react';
import {
  IFormArrayControlProps,
  IFormControlProps,
  IFormDefinition,
  IFormDefinitionProperty,
  INamedConfigurationProperty,
  IRenderFieldProps,
} from './models';
import {
  FormCheckboxComponent,
  FormDurationComponent,
  FormHiddenComponent,
  FormInputComponent,
  FormSelectComponent,
  FormTextAreaComponent,
} from './widgets';
import { FormArrayComponent } from './widgets/FormArrayComponent';

export interface IFormBuilderProps<T> {
  definition: IFormDefinition;
  initialValue: T;
  i18nRequiredProperty: string;
  customComponents: { [type: string]: any };
  children(props: IFormBuilderChildrenProps<T>): any;
}

export interface IFormBuilderChildrenProps<T> {
  propertiesArray: INamedConfigurationProperty[];
  getField: (props: IRenderFieldProps) => any;
  initialValue: T;
}

export class FormBuilder<T> extends React.Component<IFormBuilderProps<T>> {
  /**
   * Converts a property configuration to some kind of input field
   * @param props
   */
  public getField = (props: IRenderFieldProps): any => {
    // Massage the value in the definition to an input type
    const type = this.massageType(props.property);
    const componentTypemaps = {
      checkbox: FormCheckboxComponent,
      duration: FormDurationComponent,
      hidden: FormHiddenComponent,
      select: FormSelectComponent,
      textarea: FormTextAreaComponent,
      ...(this.props.customComponents || {}),
    };
    const validate = (value: T) => {
      if (props.property.required && typeof value === 'undefined') {
        return this.props.i18nRequiredProperty;
      }
      return undefined;
    };
    switch (type) {
      case 'array':
        return (
          <FieldArray
            {...props as IFormArrayControlProps}
            key={props.property.name}
            name={props.property.name}
          >
            {fieldArrayProps => (
              <FormArrayComponent
                {...props}
                {...fieldArrayProps}
                customComponents={this.props.customComponents}
              />
            )}
          </FieldArray>
        );
      default:
        return (
          <Field
            key={props.property.name}
            name={props.property.name}
            type={type}
            validate={validate}
            {...props as IFormControlProps}
            component={componentTypemaps[type] || FormInputComponent}
          />
        );
    }
  };

  public render() {
    const propertiesArray = this.enrichAndOrderProperties(
      this.props.definition
    );
    const massagedValue = this.sanitizeValues(
      this.props.definition,
      this.props.initialValue
    );
    return this.props.children({
      getField: this.getField,
      initialValue: massagedValue,
      propertiesArray,
    });
  }

  /**
   * Ensure that the input values match the property definitions
   */
  private sanitizeValues(definition: IFormDefinition, initialValue: any): T {
    return Object.keys(definition).reduce((result, key): any => {
      const prop = definition[key];
      let value = this.massageValue(prop, initialValue[key]);
      if (value == null) {
        value = this.massageValue(prop, prop.defaultValue);
      }
      return { ...result, [key]: value };
    }, {}) as T;
  }

  /**
   * Add the 'name' field from the property ID and sort them by the 'order' property
   */
  private enrichAndOrderProperties(definition: IFormDefinition) {
    return Object.keys(definition)
      .map(key => ({
        ...definition[key],
        name: key,
        required: this.massageRequired(definition[key]),
        type: this.massageType(definition[key]),
      }))
      .sort((a, b) => {
        const aOrder = (a.order || 0) as number;
        const bOrder = (b.order || 0) as number;
        return aOrder - bOrder;
      });
  }

  /**
   * Converts various values passed into the property type to known input types
   *
   * @param property
   */
  private massageType(property: IFormDefinitionProperty) {
    let type = property.type || 'text';
    switch (type) {
      case 'int':
      case 'integer':
      case 'long':
        type = 'number';
        break;
      case 'string':
        type = 'text';
        break;
      case 'boolean':
        type = 'checkbox';
    }
    if (property.enum && property.enum.length) {
      type = 'select';
    }
    if (property.secret) {
      type = 'password';
    }
    return type;
  }

  /**
   * Ensure that the 'required' property is false for checkboxes and hidden fields
   *
   * This is a candidate for removal in the future, as it's a workaround
   *
   * @param property
   */
  private massageRequired(property: IFormDefinitionProperty): any {
    switch (property.type) {
      case 'boolean':
      case 'checkbox':
      case 'hidden':
        return false;
      default:
        return property.required;
    }
  }

  /**
   * Converts the given value from a string to the type defined in the property definition
   *
   * This is a candidate for removal as it's a workaround
   *
   * @param property
   * @param value
   */
  private massageValue(property: IFormDefinitionProperty, value?: string) {
    if (value === undefined || value === null) {
      if (property.enum && property.enum.length > 0) {
        return property.enum[0].value;
      }
      return value;
    }
    switch (property.type) {
      case 'number':
        return parseInt(value, 10);
      case 'boolean':
      case 'checkbox':
        return String(value).toLocaleLowerCase() === 'true';
      default:
        return value;
    }
  }
}
