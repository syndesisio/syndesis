import { Grid } from 'patternfly-react';
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
      <Grid>
        <Grid.Row>
          <Grid.Col xs={2} className="property-label">
            {this.props.i18nName}
          </Grid.Col>
          <Grid.Col className="property-value">
            {this.props.extensionName}
          </Grid.Col>
        </Grid.Row>
        <Grid.Row>
          <Grid.Col xs={2} className="property-label">
            {this.props.i18nDescription}
          </Grid.Col>
          <Grid.Col className="property-value">
            {this.props.extensionDescription
              ? this.props.extensionDescription
              : null}
          </Grid.Col>
        </Grid.Row>
        <Grid.Row>
          <Grid.Col xs={2} className="property-label">
            {this.props.i18nType}
          </Grid.Col>
          <Grid.Col className="property-value">
            {this.props.i18nTypeMessage}
          </Grid.Col>
        </Grid.Row>
        <Grid.Row>
          <Grid.Col xs={2} className="property-label">
            {this.props.i18nLastUpdate}
          </Grid.Col>
          <Grid.Col className="property-value">
            {this.props.i18nLastUpdateDate
              ? this.props.i18nLastUpdateDate
              : null}
          </Grid.Col>
        </Grid.Row>
      </Grid>
    );
  }
}
