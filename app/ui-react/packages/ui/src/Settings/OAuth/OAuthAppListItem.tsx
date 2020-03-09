import {
  DataListCell,
  DataListContent,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  DataListToggle
} from '@patternfly/react-core';
import * as React from 'react';
import { useState } from 'react';
import { toValidHtmlId } from '../../helpers';

export interface IOAuthAppListItemProps {
  id: string;
  configured: boolean;
  children: React.ReactNode;
  expanded: boolean;
  icon: React.ReactNode;
  i18nNotConfiguredText: string;
  name: string;
}

export const OAuthAppListItem: React.FC<IOAuthAppListItemProps> = (
  {
    children,
    configured,
    i18nNotConfiguredText,
    icon,
    name
  }) => {
  const [rowExpanded, setRowExpanded] = useState(false);

  const doExpand = () => {
    setRowExpanded(!rowExpanded);
  };

  return (
    <DataListItem aria-labelledby={'app item'}
                  isExpanded={rowExpanded}
                  className={'oauth-app-list-item'}>
      <DataListItemRow data-testid={`o-auth-app-list-item-${toValidHtmlId(
        name
      )}-list-item`}>
        <DataListToggle
          onClick={doExpand}
          isExpanded={rowExpanded}
          id={'app-item-toggle'}
        />
        <DataListItemCells
          dataListCells={[
            <DataListCell id={'app-icon'} key={'icon'}  width={1}>
              {icon}
            </DataListCell>,
            <DataListCell id={'app-name'} key={'name'} width={4}>
              <b>{name}</b>
            </DataListCell>,
            <DataListCell id={'app-configured'} key={'configured'} width={5}>
              {!configured && (
                <i key={0}>{i18nNotConfiguredText}</i>
              )}
            </DataListCell>,
          ]}
        />
      </DataListItemRow>
      <DataListContent
        aria-label={'App Item Content'}
        id={'app-item'}
        isHidden={!rowExpanded}
      >
        {children}
      </DataListContent>
    </DataListItem>
  );
};
