import {
  EmptyState
} from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';

export const RestError = () => (
  <EmptyState>
    <EmptyState.Icon />
    <EmptyState.Title>Something is wrong</EmptyState.Title>
    <EmptyState.Info>
      An error occurred while talking with one of the API. Your logged in user
      doesn't have the right authorizations to interact with the APIs, or the login session expired.
    </EmptyState.Info>
    <EmptyState.Help >
      Try checking the <Link to={'/settings'}>settings page.</Link> for errors in the configuration.
    </EmptyState.Help>
    <EmptyState.Action>
      <Link to={'/logout'} className={'btn btn-lg btn-primary'}>Logout</Link>
    </EmptyState.Action>
  </EmptyState>
)