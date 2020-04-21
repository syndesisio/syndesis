import * as React from 'react';

export interface IApiConnectorSecurityFormChildrenProps {
  authUrl?: string;
  handleChangeAuthUrl: (params: string) => void;
  handleChangeSelectedType: (params: string) => void;
  handleChangeTokenUrl: (params: string) => void;

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
  selectedType?: string;
  tokenUrl?: string;
  valid: boolean;
}

export interface IApiConnectorSecurityFormProps {
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

  children(props: IApiConnectorSecurityFormChildrenProps): any;

  isValid(
    authenticationType?: string,
    authorizationUrl?: string,
    tokenUrl?: string
  ): boolean;
}

export const ApiConnectorSecurityForm: React.FunctionComponent<
  IApiConnectorSecurityFormProps
> = (
  {
    children,
    initialAccessTokenUrl,
    initialAuthorizationUrl,
    initialAuthenticationType,
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

  return children({
    authUrl,
    handleChangeAuthUrl,
    handleChangeSelectedType,
    handleChangeTokenUrl,
    selectedType,
    tokenUrl,
    valid
  });
};
