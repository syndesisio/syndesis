import { Card } from 'patternfly-react';
import * as React from 'react';
import './ApiConnectorDetailsForm.css';

export interface IApiConnectorDetailsFormProps {
  /**
   * The optional icon of the API client connector.
   */
  apiConnectorIcon?: string;

  /**
   * The name of the API client connector whose details are being shown.
   */
  apiConnectorName?: string;

  /**
   * The localized text for the icon label.
   */
  i18nIconLabel: string;

  /**
   * `true` when the connection details are being edited.
   */
  isEditing: boolean;

  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e?: any) => void;

  /**
   * The callback for when an icon file was selected from the file system.
   * @param event the event whose target contains the file being uploaded
   */
  onUploadImage: (event: any) => void;

  fields: React.ReactNode;
  footer: React.ReactNode;
}

export class ApiConnectorDetailsForm extends React.Component<
  IApiConnectorDetailsFormProps
> {
  public render() {
    return (
      <Card className="api-connector-details-form__card">
        {this.props.apiConnectorName && (
          <Card.Heading>
            <Card.Title className="api-connector-details-form__title">
              {this.props.apiConnectorName}
            </Card.Title>
          </Card.Heading>
        )}
        <Card.Body className="api-connector-details-form__body">
          <form
            className="required-pf"
            role="form"
            onSubmit={this.props.handleSubmit}
          >
            <fieldset disabled={!this.props.isEditing}>
              <div className="form-group api-connector-details-form__iconContainer">
                <label className="control-label" htmlFor="iconFileInput">
                  {this.props.i18nIconLabel}
                </label>
                <div>
                  {this.props.apiConnectorIcon ? (
                    <img
                      className="api-connector-details-form__icon"
                      src={this.props.apiConnectorIcon}
                    />
                  ) : (
                    <div className="api-connector-details-form__icon">
                      <i className="fa fa-upload" />
                    </div>
                  )}
                  <input
                    data-testid={'api-connector-details-form-icon-file-input'}
                    type="file"
                    id="iconFileInput"
                    onChange={this.props.onUploadImage}
                  />
                </div>
              </div>
              {this.props.fields}
            </fieldset>
          </form>
        </Card.Body>
        <Card.Footer>{this.props.footer}</Card.Footer>
      </Card>
    );
  }
}
