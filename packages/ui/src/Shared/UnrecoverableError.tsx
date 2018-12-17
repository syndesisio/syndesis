import { EmptyState } from 'patternfly-react';
import * as React from 'react';

export const UnrecoverableError = () => (
  <EmptyState>
    <EmptyState.Icon />
    <EmptyState.Title>Something is wrong</EmptyState.Title>
    <EmptyState.Info>
      An error occurred while talking with the server.
    </EmptyState.Info>
    <EmptyState.Help>Please check your internet connection.</EmptyState.Help>
    <EmptyState.Action>
      <a href={'.'} className={'btn btn-lg btn-primary'}>
        Refresh
      </a>
    </EmptyState.Action>
  </EmptyState>
);
