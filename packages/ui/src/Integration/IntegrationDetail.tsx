import { ListView } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailProps {
  i18n: string;
  i18nHistory: string;
  i18nTitle: string; // temporary
  i18nVersion: string;
  i18nLastPublished: string;
}

export class IntegrationDetail extends React.Component<
  IIntegrationDetailProps
> {
  public render() {
    // return <div>Integration Detail Component</div>;
    return <ListView>{this.props.children}</ListView>;
  }
}
