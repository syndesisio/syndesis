import { Dropdown, DropdownToggle } from '@patternfly/react-core';
import * as React from 'react';

export interface IAppTopMenuProps {
  username: string;
  children: any;
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
    const { children, username } = this.props;
    return (
      <Dropdown
        id="appTopMenu"
        data-testid="appTopMenu"
        isPlain={true}
        onSelect={this.onSelect}
        toggle={
          <DropdownToggle
            data-testid={'app-top-menu-user-dropdown'}
            iconComponent={null}
            onToggle={this.onToggle}
          >
            {username}
          </DropdownToggle>
        }
        isOpen={this.state.isOpen}
        dropdownItems={React.Children.toArray(children)}
      />
    );
  }
}
