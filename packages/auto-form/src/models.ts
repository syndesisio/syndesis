import { IConfigurationProperty } from '@syndesis/models';

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
  onChange?: () => void;
}

export interface IFormProperty extends IConfigurationProperty {
  disabled?: boolean;
}

export interface IFormikFormProp {
  dirty?: boolean;
  errors?: string;
  isSubmitting?: boolean;
  isValid?: boolean;
  isValidating?: boolean;
  status?: any;
  touched?: boolean;
  values?: any;
}

export interface IFormControl {
  [name: string]: any;
  field: IFormField;
  form: IFormikFormProp;
  property: IFormProperty;
  validationState?: string;
}
