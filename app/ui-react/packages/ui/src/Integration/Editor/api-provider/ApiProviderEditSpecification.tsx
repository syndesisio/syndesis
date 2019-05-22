import * as React from 'react';

export interface IApiProviderEditSpecificationProps {
  /**
   * The title
   */
  i18nTitle?: string;
}

export class ApiProviderEditSpecification extends React.Component<
  IApiProviderEditSpecificationProps
> {
  public render() {
    return <>{this.props.children}</>;
  }
}
