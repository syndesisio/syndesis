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
import { PageSection } from '../../../Layout';
import { InlineTextEdit } from '../../../Shared';
import './ViewEditHeader.css';

export interface IViewEditHeaderProps {
  /**
   * `true` if editing is enabled.
   */
  allowEditing: boolean;

  /**
   * The optional view description.
   */
  viewDescription?: string;

  /**
   * The optional view icon.
   */
  viewIcon?: string;

  /**
   * The name of the view.
   */
  viewName: string;

  /**
   * The localized text of the description label.
   */
  i18nDescriptionLabel: string;

  /**
   * The localized placeholder text of the view description.
   */
  i18nDescriptionPlaceholder: string;

  /**
   * The localized placeholder text of the view name.
   */
  i18nNamePlaceholder: string;

  /**
   * `true` when save is in progress.
   */
  isWorking: boolean;

  /**
   * The callback for when the view description should be saved.
   * @param newDescription - the new description being saved
   * @returns `true` if save was successful
   */
  onChangeDescription: (newDescription: string) => Promise<boolean>;

  /**
   * The callback for when the view name should be saved.
   * @param newName - the new name being saved
   * @returns `true` if save was successful
   */
  onChangeName: (newName: string) => Promise<boolean>;
}

/**
 * Line 1: icon, name (name not currently editable)
 * Line 2: description label and value
 */
export class ViewEditHeader extends React.Component<IViewEditHeaderProps> {
  public render() {
    return (
      <PageSection variant={'light'}>
        <Stack gutter="md">
          <Split gutter="md" className={'view-edit-header__row'}>
            {this.props.viewIcon ? (
              <div>
                <img
                  className="view-edit-header__viewIcon"
                  src={this.props.viewIcon}
                  alt={this.props.viewName}
                  width={46}
                />
              </div>
            ) : null}
            <InlineTextEdit
              className="view-edit-header__viewName"
              value={this.props.viewName}
              allowEditing={false}
              placeholder={this.props.i18nNamePlaceholder}
              isTextArea={false}
              onChange={this.props.onChangeName}
            />
          </Split>
          <TextContent>
            <TextList component={TextListVariants.dl}>
              <TextListItem
                className="view-edit-header__propertyLabel"
                component={TextListItemVariants.dt}
              >
                {this.props.i18nDescriptionLabel}
              </TextListItem>
              <TextListItem component={TextListItemVariants.dd}>
                <InlineTextEdit
                  value={this.props.viewDescription || ''}
                  allowEditing={
                    this.props.allowEditing && !this.props.isWorking
                  }
                  i18nPlaceholder={this.props.i18nDescriptionPlaceholder}
                  isTextArea={true}
                  onChange={this.props.onChangeDescription}
                />
              </TextListItem>
            </TextList>
          </TextContent>
        </Stack>
      </PageSection>
    );
  }
}
