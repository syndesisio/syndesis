import { DropdownItem } from '@patternfly/react-core';
import * as React from 'react';

export interface IPfDropdownItem {
  children: any;
}
class PfDropdownItem extends React.Component<IPfDropdownItem> {
  public render() {
    return <DropdownItem>{this.props.children}</DropdownItem>;
  }
}

export { PfDropdownItem };
