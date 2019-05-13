import { ListView } from 'patternfly-react';
import * as React from 'react';
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
  <ListView.Item
    key={integrationName}
    className={'recent-updates-item'}
    leftContent={integrationName}
    actions={integrationDate}
    description={
      <IntegrationStatus
        currentState={integrationCurrentState}
        i18nError={i18nError}
        i18nPublished={i18nPublished}
        i18nUnpublished={i18nUnpublished}
      />
    }
    stacked={false}
  />
);
