import { EmptyState, ListView } from 'patternfly-react';
import * as React from 'react';
import { PageSection } from '../../../Layout';

export interface IConnectionSchemaListProps {
  hasListData: boolean;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
}

export class ConnectionSchemaList extends React.Component<
  IConnectionSchemaListProps
> {
  public constructor(props: IConnectionSchemaListProps) {
    super(props);
  }

  public render() {
    return (
      <PageSection noPadding={true}>
        {this.props.hasListData ? (
          <ListView>{this.props.children}</ListView>
        ) : (
          <EmptyState>
            <EmptyState.Icon />
            <EmptyState.Title>
              {this.props.i18nEmptyStateTitle}
            </EmptyState.Title>
            <EmptyState.Info>{this.props.i18nEmptyStateInfo}</EmptyState.Info>
          </EmptyState>
        )}
      </PageSection>
    );
  }
}
