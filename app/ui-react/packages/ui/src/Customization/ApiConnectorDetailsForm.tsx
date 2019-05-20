import { Button, Card } from 'patternfly-react';
import * as React from 'react';
import { Loader } from '../Layout';
import './ApiConnectorDetailsForm.css';

export interface IApiConnectorDetailsFormProps {
  /**
   * The optional icon of the API client connector.
   */
  apiConnectorIcon?: string;

  /**
   * The name of the API client connector whose details are being shown.
   */
  apiConnectorName: string;

  /**
   * The localized text for the cancel button.
   */
  i18nCancelLabel: string;

  /**
   * The localized text for the edit button.
   */
  i18nEditLabel: string;

  /**
   * The localized text for the icon label.
   */
  i18nIconLabel: string;

  /**
   * The localized text for the save button.
   */
  i18nSaveLabel: string;

  /**
   * `true` when the connection details are being edited.
   */
  isEditing: boolean;

  /**
   * `true` if the parent is doing some work and this form should disable user input.
   */
  isWorking: boolean;

  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e?: any) => void;

  /**
   * The callback for editing has been canceled.
   */
  onCancelEditing: () => void;

  /**
   * The callback for start editing.
   */
  onStartEditing: () => void;

  /**
   * The callback for when an icon file was selected from the file system.
   * @param event the event whose target contains the file being uploaded
   */
  onUploadImage: (event: any) => void;
}

export class ApiConnectorDetailsForm extends React.Component<
  IApiConnectorDetailsFormProps
> {
  public render() {
    return (
      <Card className="api-connector-details-form__card">
        <Card.Heading>
          <Card.Title className="api-connector-details-form__title">
            {this.props.apiConnectorName}
          </Card.Title>
        </Card.Heading>
        <Card.Body className="api-connector-details-form__body">
          <>
            <form
              className="form-horizontal required-pf"
              role="form"
              onSubmit={this.props.handleSubmit}
            >
              <fieldset disabled={!this.props.isEditing}>
                <div className="api-connector-details-form__iconContainer">
                  <div className="col-sm-3">
                    <label className="control-label" htmlFor="iconFileInput">
                      {this.props.i18nIconLabel}
                    </label>
                  </div>
                  <div className="col-sm-9">
                    <img
                      className="col-sm-2 api-connector-details-form__icon"
                      src={this.props.apiConnectorIcon}
                    />
                    <input
                      data-testid={'api-connector-details-form-icon-file'}
                      type="file"
                      id="iconFileInput"
                      onChange={this.props.onUploadImage}
                    />
                  </div>
                </div>
                {this.props.children}
              </fieldset>
            </form>
            {this.props.isEditing ? (
              <>
                <Button
                  data-testid={'api-connector-details-form-cancel'}
                  bsStyle="default"
                  className="api-connector-details-form__editButton"
                  disabled={this.props.isWorking}
                  onClick={this.props.onCancelEditing}
                >
                  {this.props.i18nCancelLabel}
                </Button>
                <Button
                  data-testid={'api-connector-details-form-save'}
                  bsStyle="primary"
                  className="api-connector-details-form__editButton"
                  disabled={this.props.isWorking}
                  onClick={this.props.handleSubmit}
                >
                  {this.props.isWorking && <Loader size={'sm'} inline={true} />}
                  {this.props.i18nSaveLabel}
                </Button>
              </>
            ) : (
              <Button
                data-testid={'api-connector-details-form-edit'}
                bsStyle="primary"
                onClick={this.props.onStartEditing}
              >
                {this.props.i18nEditLabel}
              </Button>
            )}
          </>
        </Card.Body>
      </Card>
    );
  }
}
