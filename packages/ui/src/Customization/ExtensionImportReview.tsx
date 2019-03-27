import { Button, Grid } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, Container } from '../Layout';
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
  cancelLink: string;

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
   * @param extensionId the ID of the extension being imported/installed
   */
  onImport(extensionId: string): void;
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
      <Container data-testid="actions-container">
        {this.props.actions
          ? this.props.actions.map((action, index) =>
              index === 0 ? (
                <Grid.Col
                  key={0}
                  dangerouslySetInnerHTML={{
                    __html: this.props.i18nActionText(
                      action.name,
                      action.description
                    ),
                  }}
                />
              ) : (
                <Grid.Row key={index}>
                  <Grid.Col key={0} xs={2} />
                  <Grid.Col
                    key={1}
                    dangerouslySetInnerHTML={{
                      __html: this.props.i18nActionText(
                        action.name,
                        action.description
                      ),
                    }}
                  />
                </Grid.Row>
              )
            )
          : null}
      </Container>
    );
  }

  public handleImport() {
    return this.props.onImport(this.props.extensionId);
  }

  public render() {
    return (
      <Grid className="extension-import-review__container">
        <Grid.Row
          data-testid="title"
          className="extension-import-review__title"
        >
          {this.props.i18nTitle}
        </Grid.Row>
        <Grid.Row>
          <Grid.Col
            data-testid="id-label"
            xs={2}
            className="extension-import-review__propertyLabel"
          >
            {this.props.i18nIdLabel}
          </Grid.Col>
          <Grid.Col
            data-testid="id-value"
            className="extension-import-review__propertyValue"
          >
            {this.props.extensionId}
          </Grid.Col>
        </Grid.Row>
        <Grid.Row>
          <Grid.Col
            data-testid="name-label"
            xs={2}
            className="extension-import-review__propertyLabel"
          >
            {this.props.i18nNameLabel}
          </Grid.Col>
          <Grid.Col
            data-testid="name-value"
            className="extension-import-review__propertyValue"
          >
            {this.props.extensionName}
          </Grid.Col>
        </Grid.Row>
        <Grid.Row>
          <Grid.Col
            data-testid="description-label"
            xs={2}
            className="extension-import-review__propertyLabel"
          >
            {this.props.i18nDescriptionLabel}
          </Grid.Col>
          <Grid.Col
            data-testid="description-value"
            className="extension-import-review__propertyValue"
          >
            {this.props.extensionDescription
              ? this.props.extensionDescription
              : null}
          </Grid.Col>
        </Grid.Row>
        <Grid.Row>
          <Grid.Col
            data-testid="type-label"
            xs={2}
            className="extension-import-review__propertyLabel"
          >
            {this.props.i18nTypeLabel}
          </Grid.Col>
          <Grid.Col
            data-testid="type-value"
            className="extension-import-review__propertyValue"
          >
            {this.props.i18nExtensionTypeMessage}
          </Grid.Col>
        </Grid.Row>
        <Grid.Row>
          <Grid.Col
            data-testid="actions-label"
            xs={2}
            className="extension-import-review__propertyLabel"
          >
            {this.props.i18nActionsLabel}
          </Grid.Col>
          {this.getActions()}
        </Grid.Row>
        <Grid.Row>
          <Grid.Col xsOffset={2}>
            <Grid.Row>
              <Grid.Col>
                <Container className="extension-import-review__buttonBar">
                  <Button
                    data-testid="import-button"
                    bsStyle="primary"
                    onClick={this.handleImport}
                  >
                    {this.props.i18nImport}
                  </Button>
                  <ButtonLink
                    data-testid="cancel-button"
                    className="extension-import-review__cancelButton"
                    href={this.props.cancelLink}
                    as={'default'}
                  >
                    {this.props.i18nCancel}
                  </ButtonLink>
                </Container>
              </Grid.Col>
            </Grid.Row>
          </Grid.Col>
        </Grid.Row>
      </Grid>
    );
  }
}
