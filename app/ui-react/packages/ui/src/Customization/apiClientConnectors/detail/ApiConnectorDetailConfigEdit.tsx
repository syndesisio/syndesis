import { Form, FormGroup, TextArea, TextInput } from '@patternfly/react-core';
import * as React from 'react';
import { IApiConnectorDetailValues } from './ApiConnectorDetailBody';

export interface IApiConnectorDetailConfigEdit {
  handleOnChange: (fieldName: string, value: string) => void;

  /**
   * Property labels
   */
  i18nLabelBaseUrl: string;
  i18nLabelDescription: string;
  i18nLabelHost: string;
  i18nLabelName: string;

  i18nNameHelper: string;
  i18nRequiredText: string;

  /**
   * Initial values displayed,
   * typically set when creating the connector
   */
  properties: IApiConnectorDetailValues;
}

export const ApiConnectorDetailConfigEdit: React.FunctionComponent<IApiConnectorDetailConfigEdit> = ({
  handleOnChange,
  i18nLabelBaseUrl,
  i18nLabelDescription,
  i18nLabelHost,
  i18nLabelName,
  i18nNameHelper,
  i18nRequiredText,
  properties,
}) => {
  type IValidation = 'default' | 'error' | 'success' | undefined;
  const [isValid, setIsValid] = React.useState<IValidation>('default');

  const onChange = (value: string, event: { target: any }) => {
    const { name } = event.target;
    handleOnChange(name, value);

    const isName = name === 'name';

    if (isName) {
      if (!value) {
        setIsValid('error');
        return;
      }
      if (properties.name !== value) {
        setIsValid('success');
      }
    }
  };

  return (
    <>
      <Form isHorizontal={true} data-testid={'api-connector-details-form'}>
        {i18nRequiredText}
        <FormGroup
          label={i18nLabelName}
          isRequired={true}
          fieldId="connector-name"
          helperTextInvalid={i18nNameHelper}
          validated={isValid}
        >
          <TextInput
            value={properties.name}
            isRequired={true}
            type="text"
            id="connector-name"
            aria-describedby="horizontal-form-name-helper"
            data-testid={'api-connector-name-field'}
            name="name"
            onChange={onChange}
            validated={isValid}
          />
        </FormGroup>
        <FormGroup label={i18nLabelDescription} fieldId="connector-description">
          <TextArea
            value={properties.description}
            onChange={onChange}
            name="description"
            id="connector-description"
          />
        </FormGroup>
        <FormGroup
          label={i18nLabelHost}
          isRequired={false}
          fieldId="connector-host"
        >
          <TextInput
            value={properties.host}
            isRequired={false}
            type="text"
            id="connector-host"
            aria-describedby="horizontal-form-host-helper"
            name="host"
            onChange={onChange}
          />
        </FormGroup>
        <FormGroup
          label={i18nLabelBaseUrl}
          isRequired={false}
          fieldId="connector-baseurl"
        >
          <TextInput
            value={properties.basePath}
            isRequired={false}
            type="text"
            id="connector-baseurl"
            aria-describedby="horizontal-form-baseurl-helper"
            name="basePath"
            onChange={onChange}
          />
        </FormGroup>
      </Form>
    </>
  );
};
