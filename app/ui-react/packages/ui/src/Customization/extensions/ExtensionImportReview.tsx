import {
  Card,
  CardBody,
  Stack,
  TextContent,
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
  Title,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import { Button, Grid } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../../Layout';
import './ExtensionImportReview.css';

export interface IImportAction {
  description: string;
  name: string;
}

export interface IExtensionImportReviewProps {
  /**
   * The extension actions.
   */
  actions?: IImportAction[];

  /**
   * The href that will be navigated to when the cancel button is clicked.
   */
  cancelLink: H.LocationDescriptor;

  /**
   * The optional description of the extension.
   */
  extensionDescription?: string;

  /**
   * The ID of the extension.
   */
  extensionId: string;

  /**
   * The name of the extension.
   */
  extensionName: string;

  /**
   * The unique, not for user consumption, identifier.
   */
  extensionUid: string;

  /**
   * The localized label for the list of actions.
   */
  i18nActionsLabel: string;

  /**
   * The localized text for the cancel button.
   */
  i18nCancel: string;

  /**
   * The localized 'Description' label.
   */
  i18nDescriptionLabel: string;

  /**
   * The localized text identifying the extension type.
   */
  i18nExtensionTypeMessage: string;

  /**
   * The localized 'ID' label.
   */
  i18nIdLabel: string;

  /**
   * The localized text for the import button.
   */
  i18nImport: string;

  /**
   * The localized 'Name' label.
   */
  i18nNameLabel: string;

  /**
   * The localized title.
   */
  i18nTitle: string;

  /**
   * The localized 'Type' label.
   */
  i18nTypeLabel: string;

  /**
   * Obtains a localized message with the action name and description.
   * @param name the action name
   * @param description the action description
   */
  i18nActionText(name: string, description: string): string;

  /**
   * Callback for when the import button is clicked.
   * @param extensionUid the UID of the extension being imported/installed
   */
  onImport(extensionUid: string): void;
}

/**
 * A function component that displays the extension import review information.
 */
export const ExtensionImportReview: React.FunctionComponent<
  IExtensionImportReviewProps
> = props => {
  const getActions = (): JSX.Element => {
    if (!props.actions) {
      return <Grid.Col />;
    }

    return (
      <TextContent>
        <TextList className="extension-import-review__actions-list">
          {props.actions
            ? props.actions.map((action, index) => (
                <TextListItem
                  key={index}
                  dangerouslySetInnerHTML={{
                    __html: props.i18nActionText(
                      action.name,
                      action.description
                    ),
                  }}
                />
              ))
            : null}
        </TextList>
      </TextContent>
    );
  };

  const handleImport = () => {
    return props.onImport(props.extensionUid);
  };

  return (
    <Card className="extension-import-review">
      <CardBody>
        <Stack gutter="md">
          <Title
            headingLevel="h1"
            size="xl"
            className="extension-import-review__title"
          >
            {props.i18nTitle}
          </Title>
          <TextContent>
            <TextList component={TextListVariants.dl}>
              <TextListItem
                component={TextListItemVariants.dt}
                className="extension-import-review__propertyLabel"
              >
                {props.i18nIdLabel}
              </TextListItem>
              <TextListItem
                component={TextListItemVariants.dd}
                className="extension-import-review__propertyValue"
              >
                {props.extensionId}
              </TextListItem>
              <TextListItem
                component={TextListItemVariants.dt}
                className="extension-import-review__propertyLabel"
              >
                {props.i18nNameLabel}
              </TextListItem>
              <TextListItem
                component={TextListItemVariants.dd}
                className="extension-import-review__propertyValue"
              >
                {props.extensionName}
              </TextListItem>
              <TextListItem
                component={TextListItemVariants.dt}
                className="extension-import-review__propertyLabel"
              >
                {props.i18nDescriptionLabel}
              </TextListItem>
              <TextListItem
                component={TextListItemVariants.dd}
                className="extension-import-review__propertyValue"
              >
                {props.extensionDescription ? props.extensionDescription : null}
              </TextListItem>
              <TextListItem
                component={TextListItemVariants.dt}
                className="extension-import-review__propertyLabel"
              >
                {props.i18nTypeLabel}
              </TextListItem>
              <TextListItem
                component={TextListItemVariants.dd}
                className="extension-import-review__propertyValue"
              >
                {props.i18nExtensionTypeMessage}
              </TextListItem>
              <TextListItem
                component={TextListItemVariants.dt}
                className="extension-import-review__propertyLabel"
              >
                {props.i18nActionsLabel}
              </TextListItem>
              <TextListItem component={TextListItemVariants.dd}>
                {getActions()}
              </TextListItem>
            </TextList>
          </TextContent>
          <div className="extension-import-review__buttonBar">
            <Button bsStyle="primary" onClick={handleImport}>
              {props.i18nImport}
            </Button>
            <ButtonLink
              data-testid={'extension-import-review-cancel-button'}
              className="extension-import-review__cancelButton"
              href={props.cancelLink}
              as={'default'}
            >
              {props.i18nCancel}
            </ButtonLink>
          </div>
        </Stack>
      </CardBody>
    </Card>
  );
};
