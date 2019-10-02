import { Card, CardBody, CardFooter, CardHeader, Title } from '@patternfly/react-core';
import * as H from '@syndesis/history';
import {
  Alert,
  ControlLabel,
  FormControl,
  FormGroup,
  Radio,
} from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../../../Layout';

export function isAuthTypeValid(authType?: string): boolean {
  return authType
    ? ['apiKey', 'basic', 'none'].find(v => authType.indexOf(v) === 0) !==
        undefined
    : false;
}

export interface IAuthenticationTypes {
  value?: string;
  label?: string;
}

export interface IApiClientConnectorCreateSecurityProps {
  /**
   * Access token, required for OAuth 2.0.
   */
  accessToken?: string;
  /**
   * Used specifically for determining the default type, mostly used
   * for None and Basic types.
   */
  authenticationTypeDefault?: string;
  /**
   * The list of available authentication types for this specification.
   */
  authenticationTypes?: IAuthenticationTypes[];
  backHref: H.LocationDescriptor;
  /**
   * Authorization URL, required for OAuth 2.0.
   */
  authorizationUrl?: string;
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

  /**
   * The action fired when the user presses the Next button
   */
  onNext(
    accessToken?: string,
    authenticationType?: string,
    authorizationUrl?: string
  ): void;
}

export interface IApiClientConnectorCreateSecurityState {
  accessTokenUrl?: string;
  authorizationUrl?: string;
  selectedType?: string;
  valid: boolean;
}

export class ApiClientConnectorCreateSecurity extends React.Component<
  IApiClientConnectorCreateSecurityProps,
  IApiClientConnectorCreateSecurityState
> {
  constructor(props: any) {
    super(props);
    this.state = {
      selectedType: this.props.authenticationTypeDefault || 'none',
      valid: isAuthTypeValid(this.props.authenticationTypeDefault),
    };

    this.onSelectType = this.onSelectType.bind(this);
    this.setAccessTokenUrl = this.setAccessTokenUrl.bind(this);
    this.setAuthorizationUrl = this.setAuthorizationUrl.bind(this);
  }

  /**
   * The action fired when the user selects the authentication
   * type they want to use for the client connector.
   * @param newType
   */
  public onSelectType(newType: string) {
    this.setState({
      selectedType: newType,
      /**
       * Check if the security type is either Basic or None, in which case the form
       * should be valid.
       */
      valid: isAuthTypeValid(newType),
    });
  }

  public setAccessTokenUrl(e: React.FormEvent<HTMLInputElement>) {
    this.setState({ accessTokenUrl: e.currentTarget.value, valid: true });
  }

  public setAuthorizationUrl(e: React.FormEvent<HTMLInputElement>) {
    this.setState({ authorizationUrl: e.currentTarget.value, valid: true });
  }

  public render() {
    return (
      <Card style={{ maxWidth: '600px', margin: ' auto' }}>
        <CardHeader>
          <Title size="2xl">{this.props.i18nTitle}</Title>
        </CardHeader>
        <CardBody>
          <Alert type={'info'}>
            <span>{this.props.i18nDescription}</span>
          </Alert>
          <FormGroup controlId={'authenticationType'} disabled={false}>
            {this.props.authenticationTypes!.map(
              (authType: IAuthenticationTypes, idx) => {
                return (
                  <div key={authType.value + '-' + idx}>
                    <Radio
                      id={'authenticationType'}
                      aria-label={'Authentication Type'}
                      checked={this.state.selectedType === authType.value}
                      name={'authenticationType'}
                      onClick={() => this.onSelectType(authType.value!)}
                      readOnly={true}
                    >
                      {authType.label || this.props.i18nNoSecurity}
                    </Radio>
                  </div>
                );
              }
            )}
            {this.state.selectedType!.includes('oauth2') && (
              <>
                <FormGroup controlId={'authorizationUrl'} disabled={false}>
                  <ControlLabel>{this.props.i18nAuthorizationUrl}</ControlLabel>
                  <FormControl
                    type={'text'}
                    value={
                      this.state.authorizationUrl || this.props.authorizationUrl
                    }
                    onChange={this.setAuthorizationUrl}
                  />
                </FormGroup>
                <FormGroup controlId={'accessTokenUrl'} disabled={false}>
                  <ControlLabel>{this.props.i18nAccessTokenUrl}</ControlLabel>
                  <FormControl
                    type={'text'}
                    value={this.state.accessTokenUrl || this.props.accessToken}
                    onChange={this.setAccessTokenUrl}
                  />
                </FormGroup>
              </>
            )}
          </FormGroup>
        </CardBody>
        <CardFooter>
          <div>
            <ButtonLink href={this.props.backHref}>
              {this.props.i18nBtnBack}
            </ButtonLink>
            &nbsp;
            <ButtonLink
              onClick={() =>
                this.props.onNext(
                  this.state.accessTokenUrl,
                  this.state.selectedType,
                  this.state.authorizationUrl
                )
              }
              as={'primary'}
              disabled={!this.state.valid}
            >
              {this.props.i18nBtnNext}
            </ButtonLink>
          </div>
        </CardFooter>
      </Card>
    );
  }
}
