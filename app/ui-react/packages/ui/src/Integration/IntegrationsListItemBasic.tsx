import {
  DataListCell,
  DataListCheck,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
} from '@patternfly/react-core';
import classnames from 'classnames';
import * as React from 'react';
import { toValidHtmlId } from '../helpers';

export interface IIntegrationsListItemBasicProps {
  additionalInfo: string;
  className?: string;

  integrationName: string;
  isChecked: (name: string) => boolean;
  onCheck: (checked: boolean, name: string) => void;
}

export const IntegrationsListItemBasic: React.FunctionComponent<IIntegrationsListItemBasicProps> = ({
  additionalInfo,
  className,
  integrationName,
  isChecked,
  onCheck,
}) => {
  const id = `data-list-item-${toValidHtmlId(integrationName)}`;
  return (
    <DataListItem aria-labelledby={id} className={classnames('', className)}>
      <DataListItemRow>
        <DataListCheck
          checked={isChecked(integrationName)}
          aria-labelledby={id}
          onChange={(checked, event) => onCheck(checked, integrationName)}
        />
        <DataListItemCells
          dataListCells={[
            <DataListCell key={0}>
              <span id={id}>{integrationName}</span>
            </DataListCell>,
            <DataListCell key={1}>{additionalInfo}</DataListCell>,
          ]}
        />
      </DataListItemRow>
    </DataListItem>
  );
};
