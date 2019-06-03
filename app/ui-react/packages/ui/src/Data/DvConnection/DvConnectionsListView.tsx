import * as H from '@syndesis/history';
import { EmptyState } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, Container, PageSection } from '../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../Shared';

export interface IDvConnectionsListViewProps extends IListViewToolbarProps {
  linkToConnectionCreate: H.LocationDescriptor;
  i18nLinkCreateConnection: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
}

export class DvConnectionsListView extends React.Component<
  IDvConnectionsListViewProps
> {
  public render() {
    return (
      <PageSection noPadding={true}>
        {this.props.resultsCount > 0 ? (
          <PageSection>
            <ListViewToolbar {...this.props}>
              <div className="form-group">
                <ButtonLink
                  data-testid={
                    'dv-connections-list-view-create-connection-button'
                  }
                  href={this.props.linkToConnectionCreate}
                  as={'primary'}
                >
                  {this.props.i18nLinkCreateConnection}
                </ButtonLink>
              </div>
            </ListViewToolbar>
            <Container>{this.props.children}</Container>
          </PageSection>
        ) : (
          <EmptyState>
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
