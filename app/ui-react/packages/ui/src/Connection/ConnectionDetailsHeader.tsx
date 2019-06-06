import {
  Split,
  Stack,
  TextContent,
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
} from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../Layout';
import { InlineTextEdit } from '../Shared';
import './ConnectionDetailsHeader.css';

export interface IConnectionDetailsHeaderProps {
  /**
   * `true` if the name and description can be edited.
   */
  allowEditing: boolean;

  /**
   * The optional connection description.
   */
  connectionDescription?: string;

  /**
   * The connection icon.
   */
  connectionIcon: React.ReactNode;

  /**
   * The name of the connection.
   */
  connectionName: string;

  /**
   * The localized text of the description label.
   */
  i18nDescriptionLabel: string;

  /**
   * The localized placeholder text of the connection description.
   */
  i18nDescriptionPlaceholder: string;

  /**
   * The localized placeholder text of the connection name.
   */
  i18nNamePlaceholder: string;

  /**
   * The localized text of the usage label.
   */
  i18nUsageLabel: string;

  /**
   * The localized message that provides the number of integrations that are using this connection.
   */
  i18nUsageMessage: string;

  /**
   * `true` when the name or description is being saved.
   */
  isWorking: boolean;

  /**
   * The callback for when the connection description should be saved.
   * @param newDescription - the new description being saved
   * @returns `true` if save was successful
   */
  onChangeDescription: (newDescription: string) => Promise<boolean>;

  /**
   * The callback for when the connection name should be saved.
   * @param newName - the new name being saved
   * @returns `true` if save was successful
   */
  onChangeName: (newName: string) => Promise<boolean>;
}

/**
 * Line 1: icon, name, edit icon (makes name editable when clicked)
 * Line 2: description label and value
 * Line 3: usage label and value
 */
export class ConnectionDetailsHeader extends React.Component<
  IConnectionDetailsHeaderProps
> {
  public render() {
    return (
      <PageSection variant={'light'}>
        <Stack gutter="md">
          <Split gutter="md" className={'connection-details-header__row'}>
            {this.props.connectionIcon}
            <InlineTextEdit
              className="connection-details-header__connectionName"
              value={this.props.connectionName}
              allowEditing={this.props.allowEditing && !this.props.isWorking}
              placeholder={this.props.i18nNamePlaceholder}
              isTextArea={false}
              onChange={this.props.onChangeName}
            />
          </Split>
          <TextContent>
            <TextList component={TextListVariants.dl}>
              <TextListItem
                className="connection-details-header__propertyLabel"
                component={TextListItemVariants.dt}
              >
                {this.props.i18nDescriptionLabel}
              </TextListItem>
              <TextListItem component={TextListItemVariants.dd}>
                <InlineTextEdit
                  value={this.props.connectionDescription || ''}
                  allowEditing={
                    this.props.allowEditing && !this.props.isWorking
                  }
                  i18nPlaceholder={this.props.i18nDescriptionPlaceholder}
                  isTextArea={true}
                  onChange={this.props.onChangeDescription}
                />
              </TextListItem>
              <TextListItem
                className="connection-details-header__propertyLabel"
                component={TextListItemVariants.dt}
              >
                {this.props.i18nUsageLabel}
              </TextListItem>
              <TextListItem component={TextListItemVariants.dd}>
                {this.props.i18nUsageMessage}
              </TextListItem>
            </TextList>
          </TextContent>
        </Stack>
      </PageSection>
    );
  }
}
