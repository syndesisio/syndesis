import { DropdownItem } from '@patternfly/react-core';
import * as React from 'react';

export interface IPfDropdownItem {
  children: React.ReactNode;
  disabled?: boolean;
  onClick?(event: React.MouseEvent<any> | React.KeyboardEvent | MouseEvent): void;
}
class PfDropdownItem extends React.Component<IPfDropdownItem> {
  public render() {
    return (
      <DropdownItem
        isDisabled={this.props.disabled}
        onClick={this.props.onClick}
      >
        {this.props.children}
      </DropdownItem>
    );
  }
}

export { PfDropdownItem };
