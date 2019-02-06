import * as React from 'react';

export interface IApiConnectorInfoProps {
  apiConnectorBaseUrl?: string;
  // apiConnectorDescription?: string;
  // apiConnectorHost?: string;
  // apiConnectorName: string;
  // i18nBaseUrlLabel: string;
  // i18nDescriptionLabel: string;
  // i18nEdit: string;
  // i18nHostLabel: string;
  // i18nIconLabel: string;
  // i18nNameLabel: string;
  // i18nReviewActionsTitle: string;
}

export class ApiConnectorInfo extends React.Component<IApiConnectorInfoProps> {
  public render() {
    return <div>ApiConnectorInfo content goes here</div>;
  }
}
