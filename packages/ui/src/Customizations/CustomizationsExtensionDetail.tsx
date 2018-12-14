import * as React from 'react';

export interface IExtensionDetailProps {
  extensionId: string;
  i18nTitle: string;
}

export class CustomizationsExtensionDetail extends React.Component<
  IExtensionDetailProps
> {
  public render() {
    return <div>Extension Detail Component goes here</div>;
  }
}
