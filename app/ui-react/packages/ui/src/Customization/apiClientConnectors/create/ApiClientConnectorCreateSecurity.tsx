import {
  Alert,
  Form,
  FormGroup,
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

export interface IApiClientConnectorCreateSecurityProps {
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

export const ApiClientConnectorCreateSecurity: React.FunctionComponent<IApiClientConnectorCreateSecurityProps> = ({
  authenticationTypes,
  authUrl,
  extractAuthType,
  handleChangeAuthUrl,
  handleChangeSelectedType,
  handleChangeTokenUrl,
  i18nTitle,
  i18nAccessTokenUrl,
  i18nAuthorizationUrl,
  i18nDescription,
  i18nNoSecurity,
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
