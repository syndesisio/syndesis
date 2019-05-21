import { Dropdown, DropdownItem, DropdownToggle } from '@patternfly/react-core';
import * as React from 'react';

export interface IAppTopMenuProps {
  username: string;
  children: any;
  onSelectLogout(): void;
}

export interface IAppTopMenuState {
  isOpen: boolean;
}

/**
 * A component to show the logged in user menu.
 */
export class AppTopMenu extends React.Component<
  IAppTopMenuProps,
  IAppTopMenuState
> {
  public state = {
    isOpen: false,
  };
  public onToggle = (isOpen: boolean) => {
    this.setState({
      isOpen,
    });
  };
  public onSelect = (event: React.SyntheticEvent<HTMLDivElement, Event>) => {
    this.setState({
      isOpen: !this.state.isOpen,
    });
  };
  public render() {
    const { children, username, onSelectLogout } = this.props;
    const handleClick = (link: string) => {
      if (link.toLowerCase() === 'logout') {
        onSelectLogout();
      }
    };
    return (
      <Dropdown
        isPlain={true}
        onSelect={this.onSelect}
        toggle={
          <DropdownToggle onToggle={this.onToggle}>{username}</DropdownToggle>
        }
        isOpen={this.state.isOpen}
        dropdownItems={React.Children.toArray(children).map((child, idx) => {
          return (
            <DropdownItem
              onClick={event => {
                event.preventDefault();
                handleClick(child);
              }}
              key={idx}
            >
              {child}
            </DropdownItem>
          );
        })}
      />
    );
  }
}
