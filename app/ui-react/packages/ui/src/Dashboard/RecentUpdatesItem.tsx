import { GridItem } from '@patternfly/react-core';
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
  <>
    <GridItem className="recent-updates-item__title-container" span={4}>
      {integrationName}
    </GridItem>
    <GridItem span={4}>
      <IntegrationStatus
        currentState={integrationCurrentState}
        i18nError={i18nError}
        i18nPublished={i18nPublished}
        i18nUnpublished={i18nUnpublished}
      />
    </GridItem>
    <GridItem span={4}>{integrationDate}</GridItem>
  </>
);
