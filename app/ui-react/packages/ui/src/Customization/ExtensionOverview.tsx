import {
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
} from '@patternfly/react-core';
import * as React from 'react';
import './ExtensionOverview.css';

export interface IExtensionOverviewProps {
  /**
   * The optional description of the extension.
   */
  extensionDescription?: string;

  /**
   * The name of the extension.
   */
  extensionName: string;

  /**
   * The localized 'Description' label.
   */
  i18nDescription: string;

  /**
   * The localized 'Last Update' label.
   */
  i18nLastUpdate: string;

  /**
   * The localized last update date.
   */
  i18nLastUpdateDate?: string;

  /**
   * The localized 'Name' label.
   */
  i18nName: string;

  /**
   * The localized 'Type' label.
   */
  i18nType: string;

  /**
   * The localized type message.
   */
  i18nTypeMessage: string;
}

/**
 * A component that displays the overview section of the extension details page.
 */
export class ExtensionOverview extends React.Component<
  IExtensionOverviewProps
> {
  public render() {
    return (
      <TextList component={TextListVariants.dl}>
        <TextListItem component={TextListItemVariants.dt}>
          {this.props.i18nName}
        </TextListItem>
        <TextListItem component={TextListItemVariants.dd}>
          {this.props.extensionName}
        </TextListItem>
        <TextListItem component={TextListItemVariants.dt}>
          {this.props.i18nDescription}
        </TextListItem>
        <TextListItem component={TextListItemVariants.dd}>
          {this.props.extensionDescription
            ? this.props.extensionDescription
            : null}
        </TextListItem>
        <TextListItem component={TextListItemVariants.dt}>
          {this.props.i18nType}
        </TextListItem>
        <TextListItem component={TextListItemVariants.dd}>
          {this.props.i18nTypeMessage}
        </TextListItem>
        <TextListItem component={TextListItemVariants.dt}>
          {this.props.i18nLastUpdate}
        </TextListItem>
        <TextListItem component={TextListItemVariants.dd}>
          {this.props.i18nLastUpdateDate ? this.props.i18nLastUpdateDate : null}
        </TextListItem>
      </TextList>
    );
  }
}
