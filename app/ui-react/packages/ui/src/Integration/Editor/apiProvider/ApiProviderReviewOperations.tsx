import { PageSection } from '@patternfly/react-core';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { IListViewToolbarProps, ListViewToolbar } from '../../../Shared';

export class ApiProviderReviewOperations extends React.Component<
  IListViewToolbarProps
> {
  public render() {
    const { children, ...props } = this.props;
    return (
      <PageSection>
        <ListViewToolbar {...props} />
        <ListView>{children}</ListView>
      </PageSection>
    );
  }
}
