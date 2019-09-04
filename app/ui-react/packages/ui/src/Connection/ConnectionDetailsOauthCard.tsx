import { Card, CardBody, CardFooter, CardHeader, Title } from '@patternfly/react-core';
import * as React from 'react';
import { ButtonLink, Loader, PageSection } from '../Layout';

export interface IConnectionDetailsOauthCardProps {
  i18nTitle: string;
  i18nDescription: string;
  i18nValidateButton: string;
  i18nReconnectButton: string;
  onValidate: () => void;
  isValidating: boolean;
  onReconnect: () => void;
  isReconnecting: boolean;
}

export const ConnectionDetailsOauthCard: React.FunctionComponent<
  IConnectionDetailsOauthCardProps
> = ({
  i18nTitle,
  i18nDescription,
  i18nValidateButton,
  i18nReconnectButton,
  onValidate,
  isValidating,
  onReconnect,
  isReconnecting,
}) => (
  <PageSection>
    <Card>
      <CardHeader>
        <Title size="lg">{i18nTitle}</Title>
      </CardHeader>
      <CardBody>{i18nDescription}</CardBody>
      <CardFooter>
        <ButtonLink disabled={isValidating} onClick={onValidate}>
          {i18nValidateButton}{' '}
          {isValidating && <Loader inline={true} size={'xs'} />}
        </ButtonLink>
        &nbsp;
        <ButtonLink
          disabled={isReconnecting}
          onClick={onReconnect}
          as={'primary'}
        >
          {i18nReconnectButton}{' '}
          {isReconnecting && <Loader inline={true} size={'xs'} />}
        </ButtonLink>
      </CardFooter>
    </Card>
  </PageSection>
);
