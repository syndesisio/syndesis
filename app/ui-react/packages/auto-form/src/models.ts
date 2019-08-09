import { FieldArrayRenderProps, FieldProps, FormikActions } from 'formik';

/**
 * The top-level object that makes up a form definition, basically a map of properties
 */
export interface IFormDefinition {
  [name: string]: IFormDefinitionProperty;
}

export interface IFormValue {
  [name: string]: any;
}

export interface IFormErrors<T> {
  [name: string]: string;
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

export interface IAutoFormActions<T> extends FormikActions<T> {
  // nothing to do
}

export interface IFormArrayDefinitionOptions {
  fieldAttributes?: IFormFieldAttributes;
  formGroupAttributes?: IFormFieldAttributes;
  arrayControlAttributes?: IFormFieldAttributes;
  arrayRowTitleAttributes?: IFormFieldAttributes;
  controlLabelAttributes?: IFormFieldAttributes;
  minElements?: number;
  showSortControls?: boolean;
  rowTitle?: string;
  i18nAddElementText: string;
}

/**
 * Each item in a form definition is one of these
 */
export interface IFormDefinitionProperty {
  arrayDefinition?: IFormDefinition;
  arrayDefinitionOptions?: IFormArrayDefinitionOptions;
  connectorValue?: string;
  controlHint?: string;
  controlLabelAttributes?: IFormFieldAttributes;
  defaultValue?: string;
  deprecated?: boolean;
  description?: string;
  disabled?: boolean;
  displayName?: string;
  enum?: IFormPropertyValue[];
  extendedOptions?: { [name: string]: any };
  fieldAttributes?: IFormFieldAttributes;
  formGroupAttributes?: IFormFieldAttributes;
  generator?: string;
  group?: string;
  kind?: string;
  label?: string;
  labelHint?: string;
  mapsetKeys?: IMapsetKey[];
  mapsetValueDefinition?: IFormDefinitionProperty;
  order?: number;
  placeholder?: string;
  relation?: IFormPropertyRelation[];
  required?: boolean;
  secret?: boolean;
  tags?: string[];
  type: string;
  dataList?: string[];
}

export interface IMapsetKey {
  displayName: string;
  name: string;
}

export interface INamedConfigurationProperty extends IFormDefinitionProperty {
  name: string;
}

export interface IRenderFieldProps {
  allFieldsRequired: boolean;
  property: INamedConfigurationProperty;
  value: any;
  [name: string]: any;
}

export interface IFormControlProps<T = any> extends FieldProps {
  name: string;
  type: string;
  errors?: IFormErrors<T>;
  allFieldsRequired: boolean;
  property: INamedConfigurationProperty;
  value: T;
}

export interface IFormArrayControlProps<T = any> extends FieldArrayRenderProps {
  name: string;
  customComponents: { [type: string]: any };
  allFieldsRequired: boolean;
  property: INamedConfigurationProperty;
  value: T;
}
