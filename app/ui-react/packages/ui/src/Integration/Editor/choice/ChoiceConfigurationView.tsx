import * as H from '@syndesis/history';
import { ListView, ListViewItem } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';
import { ButtonLink } from '../../../Layout';

export interface IFlowItem {
  condition: string;
  href: H.LocationDescriptor;
}

export interface IChoiceConfigurationViewProps {
  flowItems: IFlowItem[];
  useDefaultFlow: boolean;
  defaultFlowHref?: H.LocationDescriptor;
  i18nOpenFlow: string;
  i18nOtherwise: string;
  i18nUseDefaultFlow: string;
  i18nWhen: string;
}

export class ChoiceConfigurationView extends React.Component<
  IChoiceConfigurationViewProps
> {
  public render() {
    return (
      <ListView>
        {this.props.flowItems.map((item: IFlowItem, index: number) => (
          <ListViewItem
            key={index}
            actions={
              <ButtonLink
                data-testid={`choice-view-mode-item-${toValidHtmlId(
                  item.condition
                )}-view-flow-button`}
                href={item.href}
                as="primary"
              >
                {this.props.i18nOpenFlow}
              </ButtonLink>
            }
            heading={
              <strong>
                {index + 1}. {this.props.i18nWhen}
              </strong>
            }
            description={item.condition}
            additionalInfo={[]}
          />
        ))}
        {this.props.useDefaultFlow && (
          <ListViewItem
            key={'otherwise'}
            actions={
              <ButtonLink
                data-testid="choice-view-mode-view-default-flow-button"
                href={this.props.defaultFlowHref}
                as="primary"
              >
                {this.props.i18nOpenFlow}
              </ButtonLink>
            }
            description={this.props.i18nUseDefaultFlow}
            heading={<strong>{this.props.i18nOtherwise}</strong>}
          />
        )}
      </ListView>
    );
  }
}
