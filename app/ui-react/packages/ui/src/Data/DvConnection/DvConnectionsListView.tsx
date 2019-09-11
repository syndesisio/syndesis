import * as H from '@syndesis/history';
import { EmptyState } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, PageSection } from '../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../Shared';

export interface IDvConnectionsListViewProps extends IListViewToolbarProps {
  linkToConnectionCreate: H.LocationDescriptor;
  i18nLinkCreateConnection: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
}

export const DvConnectionsListView: React.FunctionComponent<
  IDvConnectionsListViewProps
> = props => {
  return (
    <PageSection noPadding={true}>
      {props.resultsCount > 0 ? (
        <PageSection>
          <ListViewToolbar {...props}>
            <div className="form-group">
              <ButtonLink
                data-testid={
                  'dv-connections-list-view-create-connection-button'
                }
                href={props.linkToConnectionCreate}
                as={'primary'}
              >
                {props.i18nLinkCreateConnection}
              </ButtonLink>
            </div>
          </ListViewToolbar>
          <PageSection>{props.children}</PageSection>
        </PageSection>
      ) : (
        <EmptyState>
          <EmptyState.Title>{props.i18nEmptyStateTitle}</EmptyState.Title>
          <EmptyState.Info>{props.i18nEmptyStateInfo}</EmptyState.Info>
        </EmptyState>
      )}
    </PageSection>
  );
};
