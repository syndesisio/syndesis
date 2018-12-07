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
