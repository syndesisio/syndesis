import * as React from 'react';

export interface IApiConnectorDetailProps {
  apiConnectorId: string;
  i18nTitle: string;
}

export class CustomizationsApiConnectorDetail extends React.Component<
  IApiConnectorDetailProps
> {
  public render() {
    return <div>Api Connector Detail Component goes here</div>;
  }
}
