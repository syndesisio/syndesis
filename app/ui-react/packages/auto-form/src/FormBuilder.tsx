import { Field, FieldArray } from 'formik';
import * as React from 'react';
import {
  IFormArrayControlProps,
  IFormControlProps,
  IFormDefinition,
  INamedConfigurationProperty,
  IRenderFieldProps,
} from './models';
import { enrichAndOrderProperties, massageType, sanitizeValues } from './utils';
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
    const type = massageType(props.property);
    const componentTypemaps = {
      checkbox: FormCheckboxComponent,
      duration: FormDurationComponent,
      hidden: FormHiddenComponent,
      select: FormSelectComponent,
      textarea: FormTextAreaComponent,
      ...(this.props.customComponents || {}),
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
            {...props as IFormControlProps}
            component={componentTypemaps[type] || FormInputComponent}
          />
        );
    }
  };

  public render() {
    const propertiesArray = enrichAndOrderProperties(this.props.definition);
    const massagedValue = sanitizeValues(
      this.props.definition,
      this.props.initialValue
    );
    return this.props.children({
      getField: this.getField,
      initialValue: massagedValue as T,
      propertiesArray,
    });
  }
}
