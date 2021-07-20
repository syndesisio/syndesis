import {
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  Label,
  Text,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import { PUBLISHED } from '../models';

export interface IIntegrationDetailHistoryListViewItemProps {
  /**
   * If the integration is a draft, it renders a set of actionable buttons,
   * labeled 'Edit' and 'Publish'
   * If the integration is not a draft, it renders a dropdown actions menu
   * based on the IntegrationActions component
   */
  actions: any;
  /**
   * The current state of the integration.
   */
  currentState: string;
  /**
   * The last date the integration was updated.
   */
  updatedAt?: string;
  /**
   * The version of the integration deployment.
   */
  version?: number;
  /**
   * The text string for the "Running" label
   */
  i18nRunning: string;
  /**
   * The localized text for displaying the last published date.
   */
  i18nTextLastPublished?: string;
  /**
   * The localized text used to display the version of the integration.
   */
  i18nTextVersion?: string;
}

export const IntegrationDetailHistoryListViewItem: React.FunctionComponent<IIntegrationDetailHistoryListViewItemProps> = ({
  actions,
  currentState,
  updatedAt,
  version,
  i18nRunning,
  i18nTextLastPublished,
  i18nTextVersion,
}) => (
  <DataListItem aria-labelledby={'version-cell'}>
    <DataListItemRow>
      <DataListItemCells
        dataListCells={[
          <DataListCell key={1} width={2} name={'version-cell'}>
            <Title headingLevel="h6" size="md">
              {i18nTextVersion} {version}
            </Title>
          </DataListCell>,
          <DataListCell key={2} width={5}>
            <Text>
              {i18nTextLastPublished}
              {updatedAt}
            </Text>
          </DataListCell>,
          <DataListCell key={0} width={1}>
            {currentState === PUBLISHED ? (
              <Label>{i18nRunning}</Label>
            ) : (
              <>&nbsp;</>
            )}
          </DataListCell>,
        ]}
      />
      <DataListAction
        id={'integration-history-actions'}
        aria-labelledby={''}
        aria-label={''}
      >
        {actions}
      </DataListAction>
    </DataListItemRow>
  </DataListItem>
);
