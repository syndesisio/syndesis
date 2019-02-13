import { ListView } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailProps {
  integrationId: string;
  integrationDescription: string;
  integrationName: string;
  integrationStatus: string;
  integrationVersion: string;
  i18nTextBtnEdit: string;
  i18nTextBtnPublish: string;
  i18nTextDraft: string;
  i18nTextHistory: string;
  i18nTextHistoryMenuReplaceDraft: string;
  i18nTextHistoryMenuUnpublish: string;
  i18nTextLastPublished: string;
  i18nTextNoDescription: string;
  i18nTextTabActivity: string;
  i18nTextTabDetails: string;
  i18nTextTableMetrics: string;
  i18nTextTitle: string;
  i18nTextVersion: string;
}

export class IntegrationDetail extends React.Component<
  IIntegrationDetailProps
> {
  public render() {
    return (
      <>
        <div className="container-fluid">
          <h1>{this.props.integrationName}</h1>
          <div>{this.props.i18nTextTitle}</div>
          <div>{this.props.integrationDescription}</div>
        </div>
        <div className="container-fluid">
          <h2>{this.props.i18nTextHistory}</h2>
          {this.props.children ? (
            <ListView>{this.props.children}</ListView>
          ) : (
            <p>No History</p>
          )}
        </div>
      </>
    );
  }
}
