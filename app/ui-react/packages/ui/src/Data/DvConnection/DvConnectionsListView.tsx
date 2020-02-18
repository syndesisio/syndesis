import {
  EmptyState,
  EmptyStateBody,
  EmptyStateVariant,
  Title,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
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
                as={'default'}
              >
                {props.i18nLinkCreateConnection}
              </ButtonLink>
            </div>
          </ListViewToolbar>
          <PageSection>{props.children}</PageSection>
        </PageSection>
      ) : (
        <EmptyState variant={EmptyStateVariant.full}>
          <Title headingLevel="h5" size="lg">
            {props.i18nEmptyStateTitle}
          </Title>
          <EmptyStateBody>{props.i18nEmptyStateInfo}</EmptyStateBody>
          <ButtonLink
            className={
              'dv-connections-list-view-empty-create-connection-button'
            }
            data-testid={
              'dv-connections-list-view-empty-create-connection-button'
            }
            href={props.linkToConnectionCreate}
            as={'primary'}
          >
            {props.i18nLinkCreateConnection}
          </ButtonLink>
        </EmptyState>
      )}
    </PageSection>
  );
};
