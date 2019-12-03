import { Dropdown, DropdownToggle } from '@patternfly/react-core';
import * as React from 'react';

export interface IOperationsDropdownProps {
  body: React.ReactNode;
}
export const BreadcrumbDropdown: React.FunctionComponent<
  IOperationsDropdownProps
> = ({ body, children }) => {
  const [isOpen, setIsOpen] = React.useState(false);
  const closeDropdown = () => setIsOpen(false);
  const toggleDropdown = () => setIsOpen(!isOpen);
  return (
    <Dropdown
      className={'breadcrumb-dropdown'}
      onSelect={closeDropdown}
      toggle={
        <DropdownToggle onToggle={toggleDropdown}>
          {body}
        </DropdownToggle>
      }
      isOpen={isOpen}
    >
      {children}
    </Dropdown>
  );
};
