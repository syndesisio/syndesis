import * as React from 'react';

export interface IIntegrationDetailProps {
  i18nTitle: string;
}

export class IntegrationDetail extends React.Component<
  IIntegrationDetailProps
> {
  public render() {
    return <div>Integration Detail Component</div>;
  }
}
