import { Grid } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../helpers';
import { IntegrationStatus } from '../Integration';
import { IntegrationState } from '../Integration/models';
import './RecentUpdatesItem.css';

export interface IRecentUpdatesItem {
  integrationName: string;
  integrationCurrentState: IntegrationState;
  integrationDate: string;
  i18nError: string;
  i18nPublished: string;
  i18nUnpublished: string;
}

export const RecentUpdatesItem: React.FunctionComponent<IRecentUpdatesItem> = ({
  integrationName,
  integrationCurrentState,
  integrationDate,
  i18nError,
  i18nPublished,
  i18nUnpublished,
}) => (
  <Grid.Row
    xs={12}
    className={'recent-updates-item'}
    data-testid={`recent-updates-item-${toValidHtmlId(integrationName)}-row`}
  >
    <Grid.Col className="recent-updates-item__title-container" xs={5}>
      {integrationName}
    </Grid.Col>
    <Grid.Col xs={3}>
      <IntegrationStatus
        currentState={integrationCurrentState}
        i18nError={i18nError}
        i18nPublished={i18nPublished}
        i18nUnpublished={i18nUnpublished}
      />
    </Grid.Col>
    <Grid.Col xs={4}>{integrationDate}</Grid.Col>
  </Grid.Row>
);
