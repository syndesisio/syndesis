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
  password?: string;
  passwordType?: string;
  /**
   * portName & serviceName
   * are used for SOAP documents
   */
  portName?: string;
  serviceName?: string;
  tokenEndpoint?: string;
  username?: string;
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

export const ApiConnectorCreatorSecurityForm: React.FunctionComponent<IApiConnectorCreatorSecurityFormProps> = ({
  children,
  defaultValues,
}) => {
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

    if (isAuthType) {
      setValues({ ...defaultValues, [name]: value });
    } else {
      setValues({ ...values, [name]: value });
    }
  };

  return children({
    handleChange,
    handleSubmit,
    values,
  });
};
