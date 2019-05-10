import { Level, LevelItem } from '@patternfly/react-core';
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
  <Level gutter={'md'} className={'recent-updates-item'}>
    <LevelItem>{integrationName}</LevelItem>
    <LevelItem>
      <IntegrationStatus
        currentState={integrationCurrentState}
        i18nError={i18nError}
        i18nPublished={i18nPublished}
        i18nUnpublished={i18nUnpublished}
      />
    </LevelItem>
    <LevelItem>{integrationDate}</LevelItem>
  </Level>
);
