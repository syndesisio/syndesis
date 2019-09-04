import { Card, CardBody, CardFooter, CardHeader, Form, Title } from '@patternfly/react-core';
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
          <CardHeader>
            <Title size="lg" className="api-connector-details-form__title">
              {this.props.apiConnectorName}
            </Title>
          </CardHeader>
        )}
        <CardBody className="api-connector-details-form__body">
          <Form isHorizontal={true} onSubmit={this.props.handleSubmit}>
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
            </fieldset>
            {this.props.fields}
          </Form>
        </CardBody>
        <CardFooter>{this.props.footer}</CardFooter>
      </Card>
    );
  }
}
