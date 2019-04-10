import { IConfigurationProperty } from '@syndesis/models';
import { FieldProps } from 'formik';

export interface IFormDefinition {
  [name: string]: IConfigurationProperty;
}
export interface IFormValue {
  [name: string]: any;
}

export interface IFormErrors {
  [name: string]: string;
}

export interface IFormField {
  name: string;
  value?: any;
  defaultValue?: any;
  min?: number;
  max?: number;
  onChange?: () => void;
}

export interface IFormProperty extends IConfigurationProperty {
  disabled?: boolean;
}

export interface IFormControl extends FieldProps {
  name?: string;
  type?: string;
  property: IFormProperty;
}
