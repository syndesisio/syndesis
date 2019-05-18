import { FieldProps } from 'formik';

export interface IFormDefinition {
  [name: string]: IFormDefinitionProperty;
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

export interface IWhen {
  value?: string;
  id?: string;
}

export interface IFormPropertyRelation {
  when?: IWhen[];
  action?: string;
}

export interface IFormPropertyValue {
  value?: string;
  label?: string;
}

export interface IFormFieldAttributes {
  [name: string]: any;
}

export interface IFormDefinitionProperty {
  required?: boolean;
  secret?: boolean;
  disabled?: boolean;
  type: string;
  defaultValue?: string;
  displayName?: string;
  deprecated?: boolean;
  group?: string;
  label?: string;
  kind?: string;
  description?: string;
  enum?: IFormPropertyValue[];
  generator?: string;
  placeholder?: string;
  connectorValue?: string;
  relation?: IFormPropertyRelation[];
  controlHint?: string;
  labelHint?: string;
  tags?: string[];
  order?: number;
  fieldAttributes?: IFormFieldAttributes;
}

export interface IFormControl extends FieldProps {
  name: string;
  type: string;
  allFieldsRequired: boolean;
  property: IFormDefinitionProperty;
}
