import {
  Card,
  CardBody,
  CardFooter,
  CardTitle,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import { ButtonLink, Loader } from '../Layout';

export interface IConnectorAuthorizationProps {
  i18nTitle: string;
  i18nDescription: string;
  i18nConnectButton: string;
  onConnect: (e: React.MouseEvent<any>) => void;
  isConnecting: boolean;
}

export const ConnectorAuthorization: React.FunctionComponent<IConnectorAuthorizationProps> =
  ({
    i18nTitle,
    i18nDescription,
    i18nConnectButton,
    isConnecting,
    onConnect,
  }) => (
    <Card
      style={{
        margin: 'auto',
        maxWidth: 600,
      }}
    >
      <CardTitle>
        <Title size="lg" className="metrics-uptime__header" headingLevel={'h3'}>
          <div>{i18nTitle}</div>
        </Title>
      </CardTitle>
      <CardBody>
        <p>{i18nDescription}</p>
      </CardBody>
      <CardFooter>
        <ButtonLink onClick={onConnect} disabled={isConnecting} as={'primary'}>
          {i18nConnectButton}{' '}
          {isConnecting && <Loader size={'xs'} inline={true} />}
        </ButtonLink>
      </CardFooter>
    </Card>
  );
