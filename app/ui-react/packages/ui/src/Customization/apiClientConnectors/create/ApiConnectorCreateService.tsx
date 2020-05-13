import {
  Form,
  FormGroup,
  Radio,
  Stack,
  StackItem,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';

export interface IServiceAndPortTypes {
  value?: string;
  label?: string;
}

export interface IApiConnectorCreateServiceProps {
  handleChangeSelectedPort: (params: string) => void;
  handleChangeSelectedService: (params: string) => void;
  i18nPort: string;
  i18nService: string;
  i18nServicePortTitle: string;
  /**
   * The list of available services for this document.
   */
  portsAvailable?: IServiceAndPortTypes[];
  selectedPort?: string;
  selectedService?: string;
  servicesAvailable?: IServiceAndPortTypes[];
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
  portsAvailable,
  selectedPort,
  selectedService,
  servicesAvailable,
}) => (
  <Stack style={{ maxWidth: '600px' }} gutter="md">
    <StackItem>
      <Title size="2xl">{i18nServicePortTitle}</Title>
    </StackItem>
    <StackItem>
      <Form data-testid={`api-client-connector-service-ports`}>
        <FormGroup fieldId={'authenticationType'}>
          {servicesAvailable!.map((service: IServiceAndPortTypes, idx) => (
            <Radio
              key={service.value + '-' + idx}
              id={'authenticationType'}
              data-testid={`api-client-connector-service-${toValidHtmlId(
                service!.value
              )}`}
              aria-label={service.label}
              label={service.label}
              isChecked={selectedService === service.value}
              name={'service'}
              onChange={() => handleChangeSelectedService(service.value!)}
              value={service.value}
              readOnly={true}
            />
          ))}
        </FormGroup>
      </Form>
    </StackItem>
  </Stack>
);
