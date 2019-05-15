import { Card, Grid } from 'patternfly-react';
import * as React from 'react';
import './ApiConnectorReview.css';

export interface IApiConnectorReviewProps {
  apiConnectorDescription?: string;
  apiConnectorName?: string;
  errorMessages?: string[];
  i18nApiDefinitionHeading: string;
  i18nDescriptionLabel: string;
  i18nErrorsHeading?: string;
  i18nImportedHeading: string;
  i18nNameLabel: string;
  i18nOperationsHtmlMessage: string;
  i18nOperationTagHtmlMessages?: string[];
  i18nTitle: string;
  i18nValidationFallbackMessage?: string;
  i18nWarningsHeading?: string;
  warningMessages?: string[];
}

export class ApiConnectorReview extends React.Component<
  IApiConnectorReviewProps
> {
  public render() {
    return (
      <Card>
        <Card.Heading>
          <Card.Title>{this.props.i18nTitle}</Card.Title>
        </Card.Heading>
        <Card.Body>
          {this.props.i18nValidationFallbackMessage ? (
            <h5 className="api-connector-review__validationFallbackMessage">
              {this.props.i18nValidationFallbackMessage}
            </h5>
          ) : (
            <Grid>
              <Grid.Row className="api-connector-review__heading">
                {this.props.i18nApiDefinitionHeading}
              </Grid.Row>
              <Grid.Row>
                <Grid.Col className="api-connector-review__propertyName" xs={2}>
                  {this.props.i18nNameLabel}
                </Grid.Col>
                <Grid.Col>{this.props.apiConnectorName}</Grid.Col>
              </Grid.Row>
              <Grid.Row>
                <Grid.Col className="api-connector-review__propertyName" xs={2}>
                  {this.props.i18nDescriptionLabel}
                </Grid.Col>
                <Grid.Col>{this.props.apiConnectorDescription}</Grid.Col>
              </Grid.Row>
              <Grid.Row className="api-connector-review__heading">
                {this.props.i18nImportedHeading}
              </Grid.Row>
              <Grid.Row
                className="api-connector-review__message"
                dangerouslySetInnerHTML={{
                  __html: this.props.i18nOperationsHtmlMessage,
                }}
              />

              {/* tagged messages */}
              {this.props.i18nOperationTagHtmlMessages
                ? this.props.i18nOperationTagHtmlMessages.map(
                    (msg: string, index: number) => (
                      <Grid.Row
                        key={index}
                        className="api-connector-review__tagMessage"
                        dangerouslySetInnerHTML={{ __html: msg }}
                      />
                    )
                  )
                : null}

              {/* error messages */}
              {this.props.i18nErrorsHeading && this.props.errorMessages && (
                <Grid.Row className="api-connector-review__heading">
                  {this.props.i18nErrorsHeading}
                </Grid.Row>
              )}
              {this.props.errorMessages
                ? this.props.errorMessages.map(
                    (errorMsg: string, index: number) => (
                      <Grid.Row
                        key={index}
                        className="api-connector-review__message"
                      >
                        {index + 1}. {errorMsg}
                      </Grid.Row>
                    )
                  )
                : null}

              {/* warning messages */}
              {this.props.i18nWarningsHeading && this.props.warningMessages && (
                <Grid.Row className="api-connector-review__heading">
                  {this.props.i18nWarningsHeading}
                </Grid.Row>
              )}
              {this.props.warningMessages
                ? this.props.warningMessages.map(
                    (warningMsg: string, index: number) => (
                      <Grid.Row
                        key={index}
                        className="api-connector-review__message"
                      >
                        {index + 1}. {warningMsg}
                      </Grid.Row>
                    )
                  )
                : null}
            </Grid>
          )}
        </Card.Body>
      </Card>
    );
  }
}
