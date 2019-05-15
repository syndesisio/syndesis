import { DropdownItem } from '@patternfly/react-core';
import * as React from 'react';

export interface IPfDropdownItem {
  children: React.ReactNode;
}
class PfDropdownItem extends React.Component<IPfDropdownItem> {
  public render() {
    return <DropdownItem>{this.props.children}</DropdownItem>;
  }
}

export { PfDropdownItem };
