import { Card } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, Loader } from '../Layout';

export interface IConnectorAuthorizationProps {
  i18nTitle: string;
  i18nDescription: string;
  i18nConnectButton: string;
  onConnect: (e: React.MouseEvent<any>) => void;
  isConnecting: boolean;
}

export const ConnectorAuthorization: React.FunctionComponent<
  IConnectorAuthorizationProps
> = ({
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
    <Card.Heading>
      <Card.Title className="metrics-uptime__header">
        <div>{i18nTitle}</div>
      </Card.Title>
    </Card.Heading>
    <Card.Body>
      <p>{i18nDescription}</p>
    </Card.Body>
    <Card.Footer>
      <ButtonLink onClick={onConnect} disabled={isConnecting} as={'primary'}>
        {i18nConnectButton}{' '}
        {isConnecting && <Loader size={'xs'} inline={true} />}
      </ButtonLink>
    </Card.Footer>
  </Card>
);
