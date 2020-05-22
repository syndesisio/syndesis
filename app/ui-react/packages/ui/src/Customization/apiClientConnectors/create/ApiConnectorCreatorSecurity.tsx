import {
  Alert,
  Checkbox,
  Form,
  FormGroup,
  FormSelect,
  FormSelectOption,
  Stack,
  StackItem,
  TextInput,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';
import { ICreateConnectorPropsUi } from './ApiConnectorCreatorSecurityForm';

export interface IDropdownOption {
  value?: string;
  label?: string;
}

export interface IDropdownOptions {
  [key: string]: IDropdownOption[];
}

export interface II18n {
  [key: string]: string;
}

export interface IApiConnectorCreatorSecurityProps {
  /**
   * An object that contains arrays of enum key-value pairs to be
   * used in dropdowns.
   */
  authenticationTypes?: IDropdownOption[];
  dropdowns?: IDropdownOptions;
  handleChange?: (params?: any, event?: any) => void;
  errors?: any;
  i18n: II18n;
  values: ICreateConnectorPropsUi;
}

export const ApiConnectorCreatorSecurity: React.FunctionComponent<IApiConnectorCreatorSecurityProps> = ({
  dropdowns,
  handleChange,
  i18n,
  values,
}) => {
  const extractAuthType = (authType?: string): string => {
    // avoid npe
    if (typeof authType === 'undefined') {
      return 'unselected';
    }
    // mask out this special value
    if (authType === 'none') {
      return 'none';
    }
    // extract the type from the type:value scheme that this field uses
    return authType.split(':')[0];
  };

  return (
    <Stack style={{ maxWidth: '600px' }} gutter="md">
      <StackItem>
        <Title size="2xl">{i18n.title}</Title>
      </StackItem>
      <StackItem>
        <Form data-testid={`api-client-connector-auth-type-form`}>
          <Alert type={'info'} title={i18n.description} isInline={true} />
          <FormGroup
            fieldId={'authenticationType'}
            isRequired={true}
            label={i18n.authenticationType}
          >
            <FormSelect
              value={values.authenticationType}
              onChange={handleChange}
              id={'authenticationType'}
              name={'authenticationType'}
              aria-label={i18n.authenticationType}
            >
              {dropdowns!.authenticationTypes.map(
                (
                  authType: IDropdownOption,
                  idx: string | number | undefined
                ) => (
                  <FormSelectOption
                    data-testid={`api-client-connector-auth-type-${toValidHtmlId(
                      authType!.value
                    )}`}
                    key={idx}
                    value={authType.value}
                    label={authType.label || i18n.noSecurity}
                  />
                )
              )}
            </FormSelect>
          </FormGroup>

          {extractAuthType(values.authenticationType) === 'basic' && (
            <>
              <FormGroup
                fieldId={'username'}
                isRequired={true}
                label={i18n.username}
              >
                <TextInput
                  id={'username'}
                  type={'text'}
                  name={'username'}
                  value={values.username || ''}
                  onChange={handleChange}
                />
              </FormGroup>
              <FormGroup
                fieldId={'password'}
                isRequired={true}
                label={i18n.password}
              >
                <TextInput
                  id={'password'}
                  name={'password'}
                  type={'password'}
                  value={values.password || ''}
                  onChange={handleChange}
                />
              </FormGroup>
            </>
          )}

          {extractAuthType(values.authenticationType) === 'oauth2' && (
            <>
              <FormGroup
                fieldId={'authorizationEndpoint'}
                label={i18n.authorizationUrl}
              >
                <TextInput
                  id={'authorizationEndpoint'}
                  type={'text'}
                  name={'authorizationEndpoint'}
                  value={values.authorizationEndpoint || ''}
                  onChange={handleChange}
                />
              </FormGroup>
              <FormGroup fieldId={'tokenEndpoint'} label={i18n.accessTokenUrl}>
                <TextInput
                  id={'tokenEndpoint'}
                  name={'tokenEndpoint'}
                  type={'text'}
                  value={values.tokenEndpoint || ''}
                  onChange={handleChange}
                />
              </FormGroup>
            </>
          )}

          {extractAuthType(values.authenticationType) === 'ws-security-ut' && (
            <>
              <FormGroup fieldId={'addTimestamp'}>
                <Checkbox
                  label={i18n.timestamp}
                  id="addTimestamp"
                  name="addTimestamp"
                  aria-label="Timestamp"
                  isChecked={!!values.addTimestamp}
                  data-testid={'add-timestamp'}
                  onChange={handleChange}
                />
              </FormGroup>
              <FormGroup fieldId={'passwordType'} label={i18n.passwordType}>
                <FormSelect
                  value={values.passwordType}
                  onChange={handleChange}
                  id={'passwordType'}
                  name={'passwordType'}
                  aria-label={i18n.passwordType}
                >
                  {dropdowns!.passwordTypes.map(
                    (
                      passType: IDropdownOption,
                      idx: string | number | undefined
                    ) => (
                      <FormSelectOption
                        data-testid={`api-connector-password-type-${toValidHtmlId(
                          passType!.value
                        )}`}
                        key={idx}
                        value={passType.value}
                        label={passType.label!}
                      />
                    )
                  )}
                </FormSelect>
              </FormGroup>
              {(values.passwordType === 'PasswordText' ||
                values.passwordType === 'PasswordDigest') && (
                <>
                  <FormGroup fieldId={'addUsernameTokenNonce'}>
                    <Checkbox
                      label={i18n.usernameTokenNonce}
                      id="addUsernameTokenNonce"
                      name="addUsernameTokenNonce"
                      aria-label={i18n.usernameTokenNonce}
                      isChecked={!!values.addUsernameTokenNonce}
                      data-testid={'add-username-token-nonce'}
                      onChange={handleChange}
                    />
                  </FormGroup>
                  <FormGroup fieldId={'addUsernameTokenCreated'}>
                    <Checkbox
                      label={i18n.usernameTokenCreated}
                      id="addUsernameTokenCreated"
                      name="addUsernameTokenCreated"
                      aria-label={i18n.usernameTokenCreated}
                      isChecked={!!values.addUsernameTokenCreated}
                      data-testid={'add-username-token-created'}
                      onChange={handleChange}
                    />
                  </FormGroup>
                  <FormGroup
                    fieldId={'username'}
                    isRequired={true}
                    label={i18n.username}
                  >
                    <TextInput
                      id={'username'}
                      type={'text'}
                      name={'username'}
                      value={values.username || ''}
                      onChange={handleChange}
                    />
                  </FormGroup>
                  <FormGroup
                    fieldId={'password'}
                    isRequired={true}
                    label={i18n.password}
                  >
                    <TextInput
                      id={'password'}
                      name={'password'}
                      type={'password'}
                      value={values.password || ''}
                      onChange={handleChange}
                    />
                  </FormGroup>
                </>
              )}
            </>
          )}
        </Form>
      </StackItem>
    </Stack>
  );
};
