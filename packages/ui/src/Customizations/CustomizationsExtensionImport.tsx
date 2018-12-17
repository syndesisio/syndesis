import * as React from 'react';

export interface IExtensionImportProps {
  i18nTitle: string;
}

export class CustomizationsExtensionImport extends React.Component<
  IExtensionImportProps
> {
  public render() {
    return <div>Import Extension Component goes here</div>;
  }
}
