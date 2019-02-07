import { ListView } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailHistoryProps {
  i18nHistory: string;
  i18nTitle: string; // temporary
  i18nVersion: string;
  i18nLastPublished: string;
}

export class IntegrationDetailHistory extends React.Component<
  IIntegrationDetailHistoryProps
> {
  public render() {
    // return <div>Integration Detail History Component</div>;
    return <ListView>{this.props.children}</ListView>;
  }
}
