import {
  Dropdown, DropdownPosition, KebabToggle } from '@patternfly/react-core';
import * as React from 'react';
import { IMenuActions } from '../../Shared';
import { actionItem, IIntegrationAction } from './IntegrationActions';

export interface IIntegrationDetailHistoryListViewItemActionsProps {
  actions: IMenuActions[];
  integrationId: string;
}

export const IntegrationDetailHistoryListViewItemActions: React.FunctionComponent<
  IIntegrationDetailHistoryListViewItemActionsProps
> = (
  {
    actions,
    integrationId
  }) => {
  const [showDropdown, setShowDropdown] = React.useState(false);

  const toggleDropdown = () => {
    setShowDropdown(!showDropdown);
  };

  const baseIdString = 'integration-detail-history-list-view-item-actions';

  return (
    <Dropdown
      toggle={<KebabToggle onToggle={toggleDropdown} id="toggle-dropdown"/>}
      isOpen={showDropdown}
      isPlain={true}
      role={'presentation'}
      dropdownItems={actions.map((a, idx) => {
        return actionItem(a as IIntegrationAction, idx, baseIdString)
      })}
      position={DropdownPosition.right}
      className={'integration-actions__dropdown-kebab'}
      id={`integration-${integrationId}-action-menu`}
    />
  );
};
