import { Dropdown, DropdownToggle } from '@patternfly/react-core';
import * as React from 'react';

export interface IConditionsDropdownProps {
  selectedOperation: React.ReactNode;
}
export const ConditionsDropdown: React.FunctionComponent<
  IConditionsDropdownProps
> = ({ selectedOperation, children }) => {
  const [isOpen, setIsOpen] = React.useState(false);
  const closeDropdown = () => setIsOpen(false);
  const toggleDropdown = () => setIsOpen(!isOpen);
  return (
    <Dropdown
      onSelect={closeDropdown}
      toggle={
        <DropdownToggle onToggle={toggleDropdown}>
          {selectedOperation}
        </DropdownToggle>
      }
      isOpen={isOpen}
    >
      {children}
    </Dropdown>
  );
};
