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
import { ButtonLink } from '../../../Layout';

export interface IServiceAndPortTypes {
  value?: string;
  label?: string;
}

export interface IApiConnectorCreatorServiceProps {
  handleNext: (service: string, port: string) => void;
  i18nBtnNext: string;
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
export const ApiConnectorCreatorService: React.FunctionComponent<IApiConnectorCreatorServiceProps> =
  ({
    handleNext,
    i18nBtnNext,
    i18nPort,
    i18nService,
    i18nServicePortTitle,
    portName,
    portsAvailable,
    serviceName,
    servicesAvailable,
  }) => {
    const [port, setPort] = React.useState(portName);
    const [portsArray, setPortsArray] = React.useState(
      portsAvailable[serviceName]
    );
    const [service, setService] = React.useState(serviceName);

    const handleChangeSelectedPort = (params: string) => {
      setPort(params);
    };

    const handleChangeSelectedService = (params: string) => {
      setService(params);
      setPortsArray(portsAvailable[params]);
    };

    const handleClickNext = () => {
      handleNext(service, port);
    };

    return (
      <Stack style={{ maxWidth: '600px' }} hasGutter={true}>
        <StackItem>
          <Title size="lg" headingLevel={'h2'}>
            {i18nServicePortTitle}
          </Title>
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
        <StackItem>
          <ButtonLink
            id={'button-next'}
            data-testid={'button-next'}
            as={'primary'}
            onClick={handleClickNext}
          >
            {i18nBtnNext}
          </ButtonLink>
        </StackItem>
      </Stack>
    );
  };
