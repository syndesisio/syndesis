import {
  Form,
  FormGroup,
  FormSelect,
  FormSelectOption,
  Stack,
  StackItem,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';

export interface IServiceAndPortTypes {
  value?: string;
  label?: string;
}

export interface IApiConnectorCreateServiceProps {
  handleNext: (params: string) => void;
  i18nPort: string;
  i18nService: string;
  i18nServicePortTitle?: string;
  portName: string;
  /**
   * The list of available services for this document.
   */
  portsAvailable: any[];
  serviceName: string;
  servicesAvailable: string[];
}

/**
 * This component is displayed when users provide a WSDL
 * document for a SOAP service.
 * They will be prompted to choose a service and port
 * before proceeding to the next step.
 */
export const ApiConnectorCreateService: React.FunctionComponent<IApiConnectorCreateServiceProps> = ({
  handleNext,
  i18nPort,
  i18nService,
  i18nServicePortTitle,
  portName,
  portsAvailable,
  serviceName,
  servicesAvailable,
}) => {
  const [port, setPort] = React.useState(portName);
  const [service, setService] = React.useState(serviceName);

  const portsArray = portsAvailable[serviceName];

  const handleChangeSelectedPort = (params: string) => {
    setPort(params);
    handleNext(port);
  };

  const handleChangeSelectedService = (params: string) => {
    setService(params);
    handleNext(service);
  };

  return (
    <Stack style={{ maxWidth: '600px' }} gutter="md">
      <StackItem>
        <Title size="lg">{i18nServicePortTitle}</Title>
      </StackItem>
      <StackItem>
        <Form data-testid={`api-client-connector-service-ports`}>
          <>
            <FormGroup fieldId={'service'} label={i18nService}>
              <FormSelect
                value={service}
                data-testid={'api-connector-create-service-input'}
                id={'api-connector-create-service-input'}
                onChange={handleChangeSelectedService}
              >
                {servicesAvailable.map((serviceItem, idx) => (
                  <FormSelectOption
                    key={idx}
                    value={serviceItem}
                    label={serviceItem}
                  />
                ))}
              </FormSelect>
            </FormGroup>
            <FormGroup fieldId={'port'} label={i18nPort}>
              <FormSelect
                value={port}
                data-testid={'api-connector-create-port-input'}
                id={'api-connector-create-port-input'}
                onChange={handleChangeSelectedPort}
              >
                {portsArray.map(
                  (portItem: string, idx: string | number | undefined) => (
                    <FormSelectOption
                      key={idx}
                      value={portItem}
                      label={portItem}
                    />
                  )
                )}
              </FormSelect>
            </FormGroup>
          </>
        </Form>
      </StackItem>
    </Stack>
  );
};
