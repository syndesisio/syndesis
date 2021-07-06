import * as React from 'react';

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

import { toValidHtmlId } from '../../../helpers';
import { ICreateConnectorPropsUi } from './ApiConnectorCreatorSecurityForm';

export interface IDropdownOption {
  value?: string;
  label?: string;
}

export interface IDropdownOptions {
  [key: string]: IDropdownOption[];
}

export interface IApiConnectorCreatorSecurityProps {
  /**
   * An object that contains arrays of enum key-value pairs to be
   * used in dropdowns.
   */
  authenticationTypes?: IDropdownOption[];
  dropdowns?: IDropdownOptions;
  handleChange?: (params?: any, event?: any) => void;
  i18nAccessTokenUrl: string;
  i18nAuthenticationType: string;
  i18nAuthorizationUrl: string;
  i18nDescription: string;
  i18nPasswordType: string;
  i18nNoSecurity: string;
  i18nTimestamp: string;
  i18nTitle: string;
  i18nUsernameTokenCreated: string;
  i18nUsernameTokenNonce: string;
  values: ICreateConnectorPropsUi;
}

export const ApiConnectorCreatorSecurity: React.FunctionComponent<IApiConnectorCreatorSecurityProps> =
  ({
    dropdowns,
    handleChange,
    i18nAccessTokenUrl,
    i18nAuthenticationType,
    i18nAuthorizationUrl,
    i18nDescription,
    i18nPasswordType,
    i18nNoSecurity,
    i18nTimestamp,
    i18nTitle,
    i18nUsernameTokenCreated,
    i18nUsernameTokenNonce,
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
          <Title size="2xl">{i18nTitle}</Title>
        </StackItem>
        <StackItem>
          <Form data-testid={`api-client-connector-auth-type-form`}>
            <Alert type={'info'} title={i18nDescription} isInline={true} />
            <FormGroup
              fieldId={'authenticationType'}
              isRequired={true}
              label={i18nAuthenticationType}
            >
              <FormSelect
                value={values.authenticationType}
                onChange={handleChange}
                id={'authenticationType'}
                name={'authenticationType'}
                aria-label={i18nAuthenticationType}
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
                      label={authType.label || i18nNoSecurity}
                    />
                  )
                )}
              </FormSelect>
            </FormGroup>

            {extractAuthType(values.authenticationType) === 'oauth2' && (
              <>
                <FormGroup
                  fieldId={'authorizationEndpoint'}
                  label={i18nAuthorizationUrl}
                >
                  <TextInput
                    id={'authorizationEndpoint'}
                    type={'text'}
                    name={'authorizationEndpoint'}
                    value={values.authorizationEndpoint || ''}
                    onChange={handleChange}
                  />
                </FormGroup>
                <FormGroup fieldId={'tokenEndpoint'} label={i18nAccessTokenUrl}>
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

            {extractAuthType(values.authenticationType) ===
              'ws-security-ut' && (
              <>
                <FormGroup fieldId={'addTimestamp'}>
                  <Checkbox
                    label={i18nTimestamp}
                    id="addTimestamp"
                    name="addTimestamp"
                    aria-label="Timestamp"
                    isChecked={!!values.addTimestamp}
                    data-testid={'add-timestamp'}
                    onChange={handleChange}
                  />
                </FormGroup>
                <FormGroup fieldId={'passwordType'} label={i18nPasswordType}>
                  <FormSelect
                    value={values.passwordType}
                    onChange={handleChange}
                    id={'passwordType'}
                    name={'passwordType'}
                    aria-label={i18nPasswordType}
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
                        label={i18nUsernameTokenNonce}
                        id="addUsernameTokenNonce"
                        name="addUsernameTokenNonce"
                        aria-label={i18nUsernameTokenNonce}
                        isChecked={!!values.addUsernameTokenNonce}
                        data-testid={'add-username-token-nonce'}
                        onChange={handleChange}
                      />
                    </FormGroup>
                    <FormGroup fieldId={'addUsernameTokenCreated'}>
                      <Checkbox
                        label={i18nUsernameTokenCreated}
                        id="addUsernameTokenCreated"
                        name="addUsernameTokenCreated"
                        aria-label={i18nUsernameTokenCreated}
                        isChecked={!!values.addUsernameTokenCreated}
                        data-testid={'add-username-token-created'}
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
