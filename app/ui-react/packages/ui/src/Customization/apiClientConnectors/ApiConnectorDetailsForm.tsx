import { Form, Stack, StackItem, Title } from '@patternfly/react-core';
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
  fields: React.ReactNode;

  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e?: any) => void;

  /**
   * The localized text for the icon label.
   */
  i18nIconLabel: string;

  /**
   * `true` when the connection details are being edited.
   */
  isEditing: boolean;

  /**
   * The callback for when an icon file was selected from the file system.
   * @param event the event whose target contains the file being uploaded
   */
  onUploadImage: (event: any) => void;
}

export const ApiConnectorDetailsForm: React.FunctionComponent<
  IApiConnectorDetailsFormProps
> = (
  {
    apiConnectorIcon,
    apiConnectorName,
    fields,
    handleSubmit,
    i18nIconLabel,
    isEditing,
    onUploadImage
  }) => {
  return (
    <Stack className="api-connector-details-form__card" gutter={'md'}>
      {apiConnectorName && (
        <StackItem>
          <Title size="lg" className="api-connector-details-form__title">
            {apiConnectorName}
          </Title>
        </StackItem>
      )}
      <StackItem className="api-connector-details-form__body">
        <Form isHorizontal={true} onSubmit={handleSubmit}>
          <fieldset disabled={!isEditing}>
            <div className="form-group api-connector-details-form__iconContainer">
              <label className="control-label" htmlFor="iconFileInput">
                {i18nIconLabel}
              </label>
              <div>
                {apiConnectorIcon ? (
                  <img
                    className="api-connector-details-form__icon"
                    src={apiConnectorIcon}
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
                  onChange={onUploadImage}
                />
              </div>
            </div>
          </fieldset>
          {fields}
        </Form>
      </StackItem>
    </Stack>
  );
}
