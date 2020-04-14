import {
  Alert,
  Card,
  CardBody,
  CardFooter,
  CardHeader,
  Form,
  FormGroup,
  Radio,
  TextInput,
  Title,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink } from '../../../Layout';

export interface IAuthenticationTypes {
  value?: string;
  label?: string;
}

export interface IApiClientConnectorCreateSecurityProps {
  /**
   * Access token, required for OAuth 2.0.
   */
  initialAccessTokenUrl?: string;
  /**
   * Used specifically for determining the default type, mostly used
   * for None and Basic types.
   */
  initialAuthenticationType?: string;
  /**
   * Authorization URL, required for OAuth 2.0.
   */
  initialAuthorizationUrl?: string;
  /**
   * The list of available authentication types for this specification.
   */
  authenticationTypes?: IAuthenticationTypes[];
  backHref: H.LocationDescriptor;
  i18nAccessTokenUrl: string;
  i18nAuthorizationUrl: string;
  i18nBtnBack: string;
  i18nBtnNext: string;
  /**
   * Locale string for when no security is specified
   */
  i18nNoSecurity: string;
  i18nTitle: string;
  i18nDescription: string;

  extractAuthType(authType?: string): string;

  isValid(
    authenticationType?: string,
    authorizationUrl?: string,
    tokenUrl?: string
  ): boolean;

  /**
   * The action fired when the user presses the Next button
   */
  onNext(
    authenticationType?: string,
    authorizationUrl?: string,
    tokenUrl?: string
  ): void;
}

export const ApiClientConnectorCreateSecurity: React.FunctionComponent<IApiClientConnectorCreateSecurityProps> = ({
  backHref,
  extractAuthType,
  initialAccessTokenUrl,
  initialAuthorizationUrl,
  initialAuthenticationType,
  authenticationTypes,
  i18nTitle,
  i18nAccessTokenUrl,
  i18nAuthorizationUrl,
  i18nBtnBack,
  i18nBtnNext,
  i18nDescription,
  i18nNoSecurity,
  onNext,
  isValid,
}) => {
  const [tokenUrl, setTokenUrl] = React.useState(initialAccessTokenUrl);
  const [authUrl, setAuthUrl] = React.useState(initialAuthorizationUrl);
  const [selectedType, setSelectedType] = React.useState(
    initialAuthenticationType
  );
  const [valid, setValid] = React.useState(
    isValid(selectedType, authUrl, tokenUrl)
  );
  const handleChangeSelectedType = (newType: string) => {
    setSelectedType(newType);
    setValid(isValid(newType, authUrl, tokenUrl));
  };
  const handleChangeAuthUrl = (newUrl: string) => {
    setAuthUrl(newUrl);
    setValid(isValid(selectedType, newUrl, tokenUrl));
  };
  const handleChangeTokenUrl = (newUrl: string) => {
    setTokenUrl(newUrl);
    setValid(isValid(selectedType, authUrl, newUrl));
  };
  return (
    <Card style={{ maxWidth: '600px', margin: ' auto' }}>
      <CardHeader>
        <Title size="2xl">{i18nTitle}</Title>
      </CardHeader>
      <CardBody>
        <Form>
          <Alert type={'info'} title={i18nDescription} isInline={true} />
          <FormGroup fieldId={'authenticationType'}>
            {authenticationTypes!.map((authType: IAuthenticationTypes, idx) => (
              <Radio
                key={authType.value + '-' + idx}
                id={'authenticationType'}
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
      </CardBody>
      <CardFooter>
        <div>
          <ButtonLink href={backHref}>{i18nBtnBack}</ButtonLink>
          &nbsp;
          <ButtonLink
            onClick={() => onNext(selectedType, authUrl, tokenUrl)}
            as={'primary'}
            disabled={!valid}
          >
            {i18nBtnNext}
          </ButtonLink>
        </div>
      </CardFooter>
    </Card>
  );
};
