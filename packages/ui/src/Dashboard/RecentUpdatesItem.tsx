import { Grid } from 'patternfly-react';
import * as React from 'react';
import { IntegrationStatus } from '../Integration';
import { IntegrationState } from '../Integration/models';

export interface IRecentUpdatesItem {
  integrationName: string;
  integrationCurrentState: IntegrationState;
  integrationDate: number;
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
  <Grid.Row>
    <Grid.Col sm={5}>{integrationName}</Grid.Col>
    <Grid.Col sm={3}>
      <IntegrationStatus
        currentState={integrationCurrentState}
        i18nError={i18nError}
        i18nPublished={i18nPublished}
        i18nUnpublished={i18nUnpublished}
      />
    </Grid.Col>
    <Grid.Col sm={4}>{new Date(integrationDate).toLocaleString()}</Grid.Col>
  </Grid.Row>
);
