import * as React from 'react';

export interface IIntegrationDetailProps {
  integrationId: string;
  i18nBtnEdit: string;
  i18nBtnPublish: string;
  i18nDraft: string;
  i18nHistory: string;
  i18nLastPublished: string;
  i18nNoDescription: string;
  i18nTabActivity: string;
  i18nTabDetails: string;
  i18nTableMetrics: string;
  i18nTitle: string;
  i18nVersion: string;
}

export class IntegrationDetail extends React.Component<
  IIntegrationDetailProps
> {
  public render() {
    return (
      <>
        <div className="container-fluid">
          <h1>{this.props.i18nTitle}</h1>
          <div>Integration Detail Component</div>
        </div>
      </>
    );
  }
}
