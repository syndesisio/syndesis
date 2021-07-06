import * as React from 'react';

/**
 * Customizable properties in API Client Connector wizard
 */
export interface ICreateConnectorPropsUi {
  addTimestamp?: boolean;
  addUsernameTokenCreated?: boolean;
  addUsernameTokenNonce?: boolean;
  authenticationType?: string;
  authorizationEndpoint?: string;
  passwordType?: string;
  /**
   * portName & serviceName
   * are used for SOAP documents
   */
  portName?: string;
  serviceName?: string;
  tokenEndpoint?: string;
  wsdlURL?: string;
}

export interface IApiConnectorCreatorSecurityFormChildrenProps {
  handleChange?: (param?: any, event?: any) => void;
  handleSubmit?: (param?: any) => void;
  values: ICreateConnectorPropsUi;
}

export interface IApiConnectorCreatorSecurityFormProps {
  defaultValues?: any;

  children(props: IApiConnectorCreatorSecurityFormChildrenProps): any;
}

export const ApiConnectorCreatorSecurityForm: React.FunctionComponent<IApiConnectorCreatorSecurityFormProps> =
  ({ children, defaultValues }) => {
    const [values, setValues] = React.useState(defaultValues);

    const handleSubmit = (event: { preventDefault: () => void }) => {
      if (event) {
        event.preventDefault();
      }
    };

    const handleChange = (param: any, event: any) => {
      const { checked, name, type } = event.target;

      // Checkboxes require special treatment
      const isCheckbox = type === 'checkbox';
      const value = isCheckbox ? checked : event.target.value;

      // If this is a change in the authentication type,
      // clear any previous values.
      const isAuthType = name === 'authenticationType';
      let localValues: ICreateConnectorPropsUi;

      if (isAuthType) {
        /**
         * Reset values to default if we're switching
         * authentication types
         */
        localValues = { ...defaultValues, [name]: value };
      } else {
        localValues = { ...values, [name]: value };
        /**
         * Reset "password type"-specific fields if we're
         * switching password types
         */
        if (name === 'passwordType') {
          localValues.addTimestamp = undefined;
          localValues.addUsernameTokenCreated = undefined;
          localValues.addUsernameTokenNonce = undefined;
        }
      }

      setValues(() => localValues);
    };

    return children({
      handleChange,
      handleSubmit,
      values,
    });
  };
