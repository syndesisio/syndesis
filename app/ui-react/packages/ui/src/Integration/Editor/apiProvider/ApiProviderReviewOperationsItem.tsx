import { ListView } from 'patternfly-react';
import * as React from 'react';

export interface IApiProviderReviewOperationsItemProps {}

export class ApiProviderReviewOperationsItem extends React.Component<
  IApiProviderReviewOperationsItemProps
> {
  public render() {
    return <ListView.Item />;
  }
}
