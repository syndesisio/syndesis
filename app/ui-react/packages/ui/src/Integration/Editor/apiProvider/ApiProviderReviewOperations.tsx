import { DataList, PageSection } from '@patternfly/react-core';
import * as React from 'react';
import { IListViewToolbarProps, ListViewToolbar } from '../../../Shared';

export const ApiProviderReviewOperations: React.FunctionComponent<IListViewToolbarProps> = ({
  children,
  ...props
}) => (
  <PageSection>
    <ListViewToolbar {...props} />
    <DataList aria-label={'api provider review operations list'}>
      {children}
    </DataList>
  </PageSection>
);
