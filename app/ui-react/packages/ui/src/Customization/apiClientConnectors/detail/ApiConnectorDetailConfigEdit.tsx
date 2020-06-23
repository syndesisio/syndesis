import { Form, FormGroup, TextArea, TextInput } from '@patternfly/react-core';
import * as React from 'react';
import { IApiConnectorDetailValues } from './ApiConnectorDetailBody';

export interface IApiConnectorDetailConfigEdit {
  handleOnChange: (value: string, event?: any) => void;

  // Property labels
  i18nLabelBaseUrl: string;
  i18nLabelDescription: string;
  i18nLabelHost: string;
  i18nLabelName: string;

  // Initial properties
  properties: IApiConnectorDetailValues;
}

export const ApiConnectorDetailConfigEdit: React.FunctionComponent<IApiConnectorDetailConfigEdit> = ({
  handleOnChange,
  i18nLabelBaseUrl,
  i18nLabelDescription,
  i18nLabelHost,
  i18nLabelName,
  properties,
}) => {
  // tslint:disable:no-console
  const onChange = (value: string, event: { target: HTMLInputElement }) => {
    const { name } = event.target;
    handleOnChange(name, value);
  };

  return (
    <>
      <Form isHorizontal={true} data-testid={'api-connector-details-form'}>
        <FormGroup
          label={i18nLabelName}
          isRequired={true}
          fieldId="connector-name"
          helperText="Please provide a name for the API Connector"
        >
          <TextInput
            value={properties.name}
            isRequired={true}
            type="text"
            id="connector-name"
            aria-describedby="horizontal-form-name-helper"
            name="name"
            onChange={onChange}
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
          helperText="Please provide a host for the API Connector"
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
          helperText="Please provide a base URL for the API Connector"
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
