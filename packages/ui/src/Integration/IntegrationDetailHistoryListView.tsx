import { Button, Grid, ListView, ListViewItem } from 'patternfly-react';
import * as React from 'react';

import './IntegrationDetailHistoryListView.css';

export interface IIntegrationDetailHistoryListViewProps {
  integrationIsDraft: boolean;
  i18nTextBtnEdit?: string;
  i18nTextBtnPublish?: string;
  i18nTextDraft?: string;
  i18nTextHistory?: string;
}

export class IntegrationDetailHistoryListView extends React.Component<
  IIntegrationDetailHistoryListViewProps
> {
  public render() {
    return (
      <Grid
        fluid={true}
        key={1}
        className="integration-detail-history-list-view"
      >
        {this.props.integrationIsDraft ? (
          <Grid.Row className="show-grid">
            <Grid.Col xs={2} md={2} className="list-description">
              {this.props.i18nTextDraft}:
            </Grid.Col>
            <Grid.Col xs={10} md={10}>
              <ListViewItem
                key={1}
                heading={this.props.i18nTextDraft}
                actions={
                  <>
                    <Button>{this.props.i18nTextBtnPublish}</Button>
                    <Button>{this.props.i18nTextBtnEdit}</Button>
                  </>
                }
                stacked={false}
              />
            </Grid.Col>
          </Grid.Row>
        ) : null}

        {this.props.children ? (
          <Grid.Row className="show-grid">
            <Grid.Col xs={2} md={2} className="list-description">
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
    );
  }
}
