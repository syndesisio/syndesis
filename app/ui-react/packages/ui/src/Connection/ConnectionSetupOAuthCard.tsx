import * as H from '@syndesis/history';
import { Card } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../Layout';

export interface IConnectionSetupOAuthProps {
  i18nTitle: string;
  i18nDescription: string;
  i18nOAuthSettingsButton: string;
  backHref: H.LocationDescriptor;
  oauthSettingsHref: string;
}

export const ConnectionSetupOAuthCard: React.FunctionComponent<
  IConnectionSetupOAuthProps
> = ({
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
    <Card.Heading>
      <Card.Title>
        <div>{i18nTitle}</div>
      </Card.Title>
    </Card.Heading>
    <Card.Body>
      <p>{i18nDescription}</p>
    </Card.Body>
    <Card.Footer>
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
    </Card.Footer>
  </Card>
);
