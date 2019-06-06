import {
  Stack,
  TextContent,
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
  Title,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import { Button, Card, CardBody, Grid } from 'patternfly-react';
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
 * A component that displays the extension import review information.
 */
export class ExtensionImportReview extends React.Component<
  IExtensionImportReviewProps
> {
  public constructor(props: IExtensionImportReviewProps) {
    super(props);
    this.handleImport = this.handleImport.bind(this);
  }

  public getActions(): JSX.Element {
    if (!this.props.actions) {
      return <Grid.Col />;
    }

    return (
      <TextContent>
        <TextList className="extension-import-review__actions-list">
          {this.props.actions
            ? this.props.actions.map((action, index) => (
                <TextListItem
                  key={index}
                  dangerouslySetInnerHTML={{
                    __html: this.props.i18nActionText(
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
  }

  public handleImport() {
    return this.props.onImport(this.props.extensionUid);
  }

  public render() {
    return (
      <Card className="extension-import-review">
        <CardBody>
          <Stack gutter="md">
            <Title
              headingLevel="h1"
              size="xl"
              className="extension-import-review__title"
            >
              {this.props.i18nTitle}
            </Title>
            <TextContent>
              <TextList component={TextListVariants.dl}>
                <TextListItem
                  component={TextListItemVariants.dt}
                  className="extension-import-review__propertyLabel"
                >
                  {this.props.i18nIdLabel}
                </TextListItem>
                <TextListItem
                  component={TextListItemVariants.dd}
                  className="extension-import-review__propertyValue"
                >
                  {this.props.extensionId}
                </TextListItem>
                <TextListItem
                  component={TextListItemVariants.dt}
                  className="extension-import-review__propertyLabel"
                >
                  {this.props.i18nNameLabel}
                </TextListItem>
                <TextListItem
                  component={TextListItemVariants.dd}
                  className="extension-import-review__propertyValue"
                >
                  {this.props.extensionName}
                </TextListItem>
                <TextListItem
                  component={TextListItemVariants.dt}
                  className="extension-import-review__propertyLabel"
                >
                  {this.props.i18nDescriptionLabel}
                </TextListItem>
                <TextListItem
                  component={TextListItemVariants.dd}
                  className="extension-import-review__propertyValue"
                >
                  {this.props.extensionDescription
                    ? this.props.extensionDescription
                    : null}
                </TextListItem>
                <TextListItem
                  component={TextListItemVariants.dt}
                  className="extension-import-review__propertyLabel"
                >
                  {this.props.i18nTypeLabel}
                </TextListItem>
                <TextListItem
                  component={TextListItemVariants.dd}
                  className="extension-import-review__propertyValue"
                >
                  {this.props.i18nExtensionTypeMessage}
                </TextListItem>
                <TextListItem
                  component={TextListItemVariants.dt}
                  className="extension-import-review__propertyLabel"
                >
                  {this.props.i18nActionsLabel}
                </TextListItem>
                <TextListItem component={TextListItemVariants.dd}>
                  {this.getActions()}
                </TextListItem>
              </TextList>
            </TextContent>
            <div className="extension-import-review__buttonBar">
              <Button bsStyle="primary" onClick={this.handleImport}>
                {this.props.i18nImport}
              </Button>
              <ButtonLink
                data-testid={'extension-import-review-cancel-button'}
                className="extension-import-review__cancelButton"
                href={this.props.cancelLink}
                as={'default'}
              >
                {this.props.i18nCancel}
              </ButtonLink>
            </div>
          </Stack>
        </CardBody>
      </Card>
    );
  }
}
