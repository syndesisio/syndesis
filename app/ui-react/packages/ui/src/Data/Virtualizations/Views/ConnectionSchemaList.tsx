import { EmptyState, ListView } from 'patternfly-react';
import * as React from 'react';
import { PageSection } from '../../../Layout';

export interface IConnectionSchemaListProps {
  hasListData: boolean;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
}

export const ConnectionSchemaList: React.FunctionComponent<
  IConnectionSchemaListProps
> = props => {

  return (
    <PageSection noPadding={true}>
      {props.hasListData ? (
        <ListView>{props.children}</ListView>
      ) : (
          <EmptyState>
            <EmptyState.Title>
              {props.i18nEmptyStateTitle}
            </EmptyState.Title>
            <EmptyState.Info>{props.i18nEmptyStateInfo}</EmptyState.Info>
          </EmptyState>
        )}
    </PageSection>
  );
}
