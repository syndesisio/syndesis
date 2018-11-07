import * as React from 'react';

export interface IAuthContext {
  authorizationUri: string;
  clientId: string;
  logged: boolean;
  redirectUri: string;
  responseType: 'token';
  token: string | null;

  updateToken(token: string): void;
}

export const AuthContextDefaultValue = {
  authorizationUri: 'http://example.com/oauth/authorize',
  clientId: 'example-client-id',
  logged: false,
  redirectUri: 'http://localhost/login',
  responseType: 'token',
  token: null
} as IAuthContext;

export const AuthContext = React.createContext<IAuthContext>(
  AuthContextDefaultValue
);
