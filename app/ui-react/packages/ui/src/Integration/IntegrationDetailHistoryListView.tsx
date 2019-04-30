import { Grid, ListView, ListViewItem } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../Layout';
import { IIntegrationAction } from './IntegrationActions';
import './IntegrationDetailHistoryListView.css';

export interface IIntegrationDetailHistoryListViewProps {
  actions?: IIntegrationAction[];
  hasHistory: boolean;
  isDraft: boolean;
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
        {this.props.isDraft ? (
          <Grid.Row className="show-grid">
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
                  this.props.actions
                    ? this.props.actions.map((a, idx) => (
                        <ButtonLink key={idx} to={a.href} onClick={a.onClick}>
                          {a.label}
                        </ButtonLink>
                      ))
                    : null
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
    );
  }
}
