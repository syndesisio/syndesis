import { EmptyState } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../Layout';

export const UnrecoverableError = () => (
  <EmptyState>
    <EmptyState.Icon />
    <EmptyState.Title>Something is wrong</EmptyState.Title>
    <EmptyState.Info>
      An error occurred while talking with the server.
    </EmptyState.Info>
    <EmptyState.Help>Please check your internet connection.</EmptyState.Help>
    <EmptyState.Action>
      <ButtonLink href={'.'} as={'primary'} size={'lg'}>
        Refresh
      </ButtonLink>
    </EmptyState.Action>
  </EmptyState>
);
