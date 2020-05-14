import {
  Form,
  FormGroup,
  FormSelect,
  FormSelectOption,
  Stack,
  StackItem,
  TextInput,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
// import { toValidHtmlId } from '../../../helpers';

export interface IServiceAndPortTypes {
  value?: string;
  label?: string;
}

export interface IApiConnectorCreateServiceProps {
  handleChangeSelectedPort: (params: string) => void;
  handleChangeSelectedService: (params: string) => void;
  i18nPort?: string;
  i18nService?: string;
  i18nServicePortTitle?: string;
  portName?: string;
  /**
   * The list of available services for this document.
   */
  portsAvailable: any[];
  serviceName?: string;
  servicesAvailable: any[];
}

/**
 * This component is displayed when users provide a WSDL
 * document for a SOAP service.
 * They will be prompted to choose a service and port
 * before proceeding to the next step.
 */
export const ApiConnectorCreateService: React.FunctionComponent<IApiConnectorCreateServiceProps> = ({
  handleChangeSelectedPort,
  handleChangeSelectedService,
  i18nPort,
  i18nService,
  i18nServicePortTitle,
  portName,
  portsAvailable,
  serviceName,
  servicesAvailable,
}) => {
  // tslint:disable:no-console
  console.log('CreateSvc portName: ' + JSON.stringify(portName));
  console.log('CreateSvc portsAvailable: ' + JSON.stringify(portsAvailable));
  console.log('CreateSvc serviceName: ' + JSON.stringify(serviceName));
  console.log(
    'CreateSvc servicesAvailable: ' + JSON.stringify(servicesAvailable)
  );

  return (
    <Stack style={{ maxWidth: '600px' }} gutter="md">
      <StackItem>
        <Title size="2xl">{i18nServicePortTitle}</Title>
      </StackItem>
      <StackItem>
        <Form data-testid={`api-client-connector-service-ports`}>
          <>
            <FormGroup fieldId={'service'} label={i18nService}>
              <FormSelect
                value={serviceName}
                data-testid={'api-connector-create-service-input'}
                id={'api-connector-create-service-input'}
                onChange={handleChangeSelectedService}
              >
                {servicesAvailable.map((service, idx) => (
                  <FormSelectOption key={idx} value={service} label={service} />
                ))}
              </FormSelect>
              <TextInput
                id={'portName'}
                type={'text'}
                value={portName}
                onChange={value => handleChangeSelectedPort(value)}
              />
            </FormGroup>
          </>
        </Form>
      </StackItem>
    </Stack>
  );
};
