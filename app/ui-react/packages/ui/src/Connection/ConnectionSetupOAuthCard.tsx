import {
  Card,
  CardBody,
  CardFooter,
  CardTitle,
  Title,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink } from '../Layout';

export interface IConnectionSetupOAuthProps {
  i18nTitle: string;
  i18nDescription: string;
  i18nOAuthSettingsButton: string;
  backHref: H.LocationDescriptor;
  oauthSettingsHref: string;
}

export const ConnectionSetupOAuthCard: React.FunctionComponent<IConnectionSetupOAuthProps> =
  ({
    i18nTitle,
    i18nDescription,
    i18nOAuthSettingsButton,
    backHref,
    oauthSettingsHref,
  }) => (
    <Card
      style={{
        margin: 'auto',
        maxWidth: 600,
      }}
    >
      <CardTitle>
        <Title size="lg" headingLevel={'h3'}>
          <div>{i18nTitle}</div>
        </Title>
      </CardTitle>
      <CardBody>
        <p>{i18nDescription}</p>
      </CardBody>
      <CardFooter>
        <ButtonLink
          data-testid={'connection-creator-back-button'}
          href={backHref}
          className={'wizard-pf-back'}
        >
          <i className="fa fa-angle-left" /> Back
        </ButtonLink>
        &nbsp;
        <ButtonLink
          data-testid={'connection-creator-settings'}
          href={oauthSettingsHref}
          as={'primary'}
        >
          {i18nOAuthSettingsButton}
        </ButtonLink>
      </CardFooter>
    </Card>
  );
