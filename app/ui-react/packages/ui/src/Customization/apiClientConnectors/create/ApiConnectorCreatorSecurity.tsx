import {
  Alert,
  Form,
  FormGroup,
  FormSelect,
  FormSelectOption,
  Radio,
  Stack,
  StackItem,
  TextInput,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';

export interface IAuthenticationTypes {
  value?: string;
  label?: string;
}

export interface IApiConnectorCreatorSecurityProps {
  /**
   * The list of available authentication types for this specification.
   */
  authenticationTypes?: IAuthenticationTypes[];
  authUrl?: string;
  handleChangeAuthUrl: (params: string) => void;
  handleChangeSelectedType: (params: string) => void;
  handleChangeTokenUrl: (params: string) => void;
  i18nAccessTokenUrl: string;
  i18nAuthorizationUrl: string;
  i18nAuthTypeLabel: string;
  i18nDescription: string;
  /**
   * Locale string for when no security is specified
   */
  i18nNoSecurity: string;
  i18nTitle: string;
  selectedType?: string;
  tokenUrl?: string;
  extractAuthType(authType?: string): string;
}

export const ApiConnectorCreatorSecurity: React.FunctionComponent<IApiConnectorCreatorSecurityProps> = ({
  authenticationTypes,
  authUrl,
  extractAuthType,
  handleChangeAuthUrl,
  handleChangeSelectedType,
  handleChangeTokenUrl,
  i18nAccessTokenUrl,
  i18nAuthorizationUrl,
  i18nAuthTypeLabel,
  i18nDescription,
  i18nNoSecurity,
  i18nTitle,
  selectedType,
  tokenUrl,
}) => {
  return (
    <Stack style={{ maxWidth: '600px' }} gutter="md">
      <StackItem>
        <Title size="2xl">{i18nTitle}</Title>
      </StackItem>
      <StackItem>
        <Form data-testid={`api-client-connector-auth-type-form`}>
          <Alert type={'info'} title={i18nDescription} isInline={true} />
          <FormGroup fieldId={'authenticationType'}>
            {authenticationTypes!.map((authType: IAuthenticationTypes, idx) => (
              <Radio
                key={authType.value + '-' + idx}
                id={'authenticationType'}
                data-testid={`api-client-connector-auth-type-${toValidHtmlId(
                  authType!.value
                )}`}
                aria-label={authType.label || i18nNoSecurity}
                label={authType.label || i18nNoSecurity}
                isChecked={selectedType === authType.value}
                name={'authenticationType'}
                onChange={() => handleChangeSelectedType(authType.value!)}
                value={authType.value}
                readOnly={true}
              />
            ))}
          </FormGroup>
          <FormGroup
            fieldId={'authenticationType'}
            isRequired={true}
            label={i18nAuthTypeLabel}
          >
            <FormSelect
              value={selectedType}
              onChange={handleChangeSelectedType}
              id={'authenticationType'}
              name={'authenticationType'}
              aria-label={i18nAuthTypeLabel}
            >
              {authenticationTypes!.map(
                (authType: IAuthenticationTypes, idx) => (
                  <FormSelectOption
                    key={idx}
                    value={authType.value}
                    label={authType.label || i18nNoSecurity}
                  />
                )
              )}
            </FormSelect>
          </FormGroup>
          {extractAuthType(selectedType) === 'oauth2' && (
            <>
              <FormGroup
                fieldId={'authorizationUrl'}
                label={i18nAuthorizationUrl}
              >
                <TextInput
                  id={'authorizationUrl'}
                  type={'text'}
                  value={authUrl}
                  onChange={value => handleChangeAuthUrl(value)}
                />
              </FormGroup>
              <FormGroup fieldId={'accessTokenUrl'} label={i18nAccessTokenUrl}>
                <TextInput
                  id={'accessTokenUrl'}
                  type={'text'}
                  value={tokenUrl}
                  onChange={value => handleChangeTokenUrl(value)}
                />
              </FormGroup>
            </>
          )}
        </Form>
      </StackItem>
    </Stack>
  );
};
