import * as H from '@syndesis/history';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../../../Layout';
import { HttpMethodColors } from '../../../Shared';

export interface IApiProviderReviewOperationsItemProps {
  createAsPrimary: boolean;
  createFlowHref: H.LocationDescriptor;
  i18nCreateFlow: string;
  onCreateFlow?: (e: React.MouseEvent<any>) => void;
  operationDescription: string;
  operationHttpMethod: string;
  operationPath: string;
}

export class ApiProviderReviewOperationsItem extends React.Component<
  IApiProviderReviewOperationsItemProps
> {
  public render() {
    return (
      <ListView.Item
        actions={
          <ButtonLink
            data-testid={'api-provider-operations-create-flow'}
            onClick={this.props.onCreateFlow}
            href={this.props.createFlowHref}
            as={this.props.createAsPrimary ? 'primary' : 'default'}
          >
            {this.props.i18nCreateFlow}
          </ButtonLink>
        }
        className={'api-provider-operations-list-item'}
        heading={this.props.operationPath}
        description={this.props.operationDescription}
        leftContent={
          <HttpMethodColors method={this.props.operationHttpMethod} />
        }
        stacked={false}
      />
    );
  }
}
