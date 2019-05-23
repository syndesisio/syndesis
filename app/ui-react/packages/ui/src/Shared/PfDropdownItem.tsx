import { DropdownItem } from '@patternfly/react-core';
import * as React from 'react';

export interface IPfDropdownItem {
  children: React.ReactNode;
  onClick?(): void;
}
class PfDropdownItem extends React.Component<IPfDropdownItem> {
  public render() {
    return (
      <DropdownItem onClick={this.props.onClick}>
        {this.props.children}
      </DropdownItem>
    );
  }
}

export { PfDropdownItem };
