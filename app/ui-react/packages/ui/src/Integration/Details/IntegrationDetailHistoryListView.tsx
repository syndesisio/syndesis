import * as H from '@syndesis/history';
import { Grid, ListView, ListViewItem } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, PageSection } from '../../Layout';
import './IntegrationDetailHistoryListView.css';

export interface IIntegrationDetailHistoryListViewProps {
  editHref?: H.LocationDescriptor;
  editLabel?: string | JSX.Element;
  hasHistory: boolean;
  isDraft: boolean;
  i18nTextDraft?: string;
  i18nTextHistory?: string;
  publishAction?: (e: React.MouseEvent<any>) => any;
  publishHref?: H.LocationDescriptor;
  publishLabel?: string | JSX.Element;
}

export class IntegrationDetailHistoryListView extends React.Component<
  IIntegrationDetailHistoryListViewProps
> {
  public render() {
    return (
      <PageSection>
        <Grid
          fluid={true}
          key={1}
          className="integration-detail-history-list-view"
        >
          {this.props.isDraft ? (
            <Grid.Row className="show-grid integration-detail-history-list-view__draft-row">
              <Grid.Col
                xs={2}
                md={2}
                className="integration-detail-history-list-view__description"
              >
                {this.props.i18nTextDraft}:
              </Grid.Col>
              <Grid.Col xs={10} md={10}>
                <ListViewItem
                  key={1}
                  heading={this.props.i18nTextDraft}
                  actions={
                    <>
                      <ButtonLink
                        data-testid={
                          'integration-detail-history-list-view-publish-button'
                        }
                        to={this.props.publishHref}
                        onClick={this.props.publishAction}
                        children={this.props.publishLabel}
                      />
                      <ButtonLink
                        data-testid={
                          'integration-detail-history-list-view-edit-button'
                        }
                        href={this.props.editHref}
                        children={this.props.editLabel}
                      />
                    </>
                  }
                  stacked={false}
                />
              </Grid.Col>
            </Grid.Row>
          ) : null}

          {this.props.children && this.props.hasHistory ? (
            <Grid.Row className="show-grid">
              <Grid.Col
                xs={2}
                md={2}
                className="integration-detail-history-list-view__description"
              >
                {<span>{this.props.i18nTextHistory}:</span>}
              </Grid.Col>
              <Grid.Col xs={10} md={10}>
                {this.props.children ? (
                  <ListView>{this.props.children}</ListView>
                ) : null}
              </Grid.Col>
            </Grid.Row>
          ) : null}
        </Grid>
      </PageSection>
    );
  }
}
