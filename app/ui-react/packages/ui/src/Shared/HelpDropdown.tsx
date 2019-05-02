import {
  Dropdown,
  DropdownDirection,
  DropdownItem,
  DropdownPosition,
  DropdownToggle,
} from '@patternfly/react-core';
import { HelpIcon } from '@patternfly/react-icons';
import classNames from 'classnames';
import * as React from 'react';

export interface IHelpDropdownProps {
  className?: string;
  isOpen: boolean;
  launchAboutModal: any;
}

export interface IHelpDropdownState {
  isOpen: boolean;
}

export class HelpDropdown extends React.Component<
  IHelpDropdownProps,
  IHelpDropdownState
> {
  public onToggle: any;
  public onSelect: any;

  constructor(props: IHelpDropdownProps) {
    super(props);
    this.state = {
      isOpen: false,
    };
    this.onToggle = (isOpen: boolean) => {
      this.setState({
        isOpen,
      });
    };
    this.onSelect = (event: MouseEvent) => {
      this.setState({
        isOpen: !this.state.isOpen,
      });
    };
  }

  public render() {
    const { isOpen } = this.state;
    const dropdownItems = [
      <DropdownItem
        key="action"
        component="span"
        onClick={() => {
          this.props.launchAboutModal();
        }}
      >
        About
      </DropdownItem>,
    ];
    return (
      <>
        <Dropdown
          direction={DropdownDirection.down}
          position={DropdownPosition.right}
          onSelect={this.onSelect}
          toggle={
            <DropdownToggle
              className={classNames('', this.props.className)}
              iconComponent={null}
              onToggle={this.onToggle}
            >
              <HelpIcon />
            </DropdownToggle>
          }
          isOpen={isOpen}
          isPlain={true}
          dropdownItems={dropdownItems}
        />
      </>
    );
  }
}
