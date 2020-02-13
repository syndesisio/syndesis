import {
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
} from '@patternfly/react-core';
import * as React from 'react';
import { toValidHtmlId } from '../helpers';
import { ButtonLink } from '../Layout';

export interface IIntegrationsListItemUnreadableProps {
  integrationName: string;
  i18nDescription: string;
  rawObject: string;
}

export const IntegrationsListItemUnreadable: React.FC<IIntegrationsListItemUnreadableProps> = ({
  integrationName,
  i18nDescription,
  rawObject,
}) => {
  const onClick = () => window.alert(rawObject);
  const id = `data-list-item-${toValidHtmlId(integrationName)}`;
  return (
    <DataListItem aria-labelledby={id}>
      <DataListItemRow>
        <DataListItemCells
          dataListCells={[
            <DataListCell key={0}>
              <span id={id}>{integrationName}</span>
            </DataListCell>,
            <DataListCell key={1}>{i18nDescription}</DataListCell>,
          ]}
        />
        <DataListAction
          id={`${id}-action`}
          aria-label={`${integrationName} button`}
          aria-labelledby={id}
        >
          <ButtonLink
            data-testid={'integrations-list-item-unreadable-json-button'}
            onClick={onClick}
          >
            Integration JSON
          </ButtonLink>
        </DataListAction>
      </DataListItemRow>
    </DataListItem>
  );
};
