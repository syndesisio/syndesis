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
    return (
      <>
        <div>{this.props.i18nTitle}</div>
        <div>{this.props.children}</div>
      </>
    );
  }
}
