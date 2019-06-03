import * as H from '@syndesis/history';
import { PropertyValue } from '@syndesis/models';
import {
  Card,
  ControlLabel,
  FormControl,
  FormGroup,
  Radio,
} from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../../../Layout';

export interface IApiClientConnectorCreateSecurityProps {
  /**
   * Access token, required for OAuth 2.0.
   */
  accessToken?: string;
  /**
   * The type of authentication selected.
   * Optional, though required in OpenAPI 2.0 for the security scheme object.
   * Valid values are "basic", "apiKey" or "oauth2".
   */
  authenticationType?: string;
  /**
   * The list of available authentication types for this specification.
   */
  authenticationTypes?: PropertyValue[];
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
      selectedType: 'basic',
      valid: false,
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
      valid: newType === 'basic',
    });
  }

  public setAccessTokenUrl(e: React.FormEvent<HTMLInputElement>) {
    this.setState({ accessTokenUrl: e.currentTarget.value });
  }

  public setAuthorizationUrl(e: React.FormEvent<HTMLInputElement>) {
    this.setState({ authorizationUrl: e.currentTarget.value });
  }

  public render() {
    return (
      <Card style={{ maxWidth: '600px' }}>
        <Card.Heading>
          <Card.Title>{this.props.i18nTitle}</Card.Title>
        </Card.Heading>
        <Card.Body>
          <FormGroup controlId={'authenticationType'} disabled={false}>
            {this.props.authenticationTypes!.map(
              (authType: PropertyValue, idx) => {
                return (
                  <div key={authType.value}>
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
            {this.state.selectedType && this.state.selectedType === 'oauth2' && (
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
                    value={this.state.accessTokenUrl}
                    onChange={this.setAccessTokenUrl}
                  />
                </FormGroup>
              </>
            )}
          </FormGroup>
        </Card.Body>
        <Card.Footer>
          <div>
            <ButtonLink href={this.props.backHref}>
              {this.props.i18nBtnBack}
            </ButtonLink>
            &nbsp;
            <ButtonLink onClick={this.props.onNext} as={'primary'}>
              {this.props.i18nBtnNext}
            </ButtonLink>
          </div>
        </Card.Footer>
      </Card>
    );
  }
}
