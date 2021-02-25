import { ICreateConnectorPropsUi } from './ApiConnectorCreatorSecurityForm';

export interface IErrorValidation {
  password?: string;
  username?: string;
}

export default function validateSecurity(values: ICreateConnectorPropsUi) {
  const errors: IErrorValidation = {};

  if (
    values.authenticationType === 'basic' ||
    values.authenticationType === 'basic:username_password' ||
    values.authenticationType === 'ws-security-ut'
  ) {
    if (values.passwordType !== 'PasswordNone') {
      if (!values.username) {
        errors.username = 'Username is required';
      }

      if (!values.password) {
        errors.password = 'Password is required';
      }
    }
  }

  return errors;
}
