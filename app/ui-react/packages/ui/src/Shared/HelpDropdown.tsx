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
  launchAboutModal: () => void;
  launchSupportPage: () => void;
  launchSampleIntegrationTutorials: () => void;
  launchUserGuide: () => void;
  launchConnectorsGuide: () => void;
  launchContactUs: () => void;
}

export interface IHelpDropdownState {
  isOpen: boolean;
}

export class HelpDropdown extends React.Component<
  IHelpDropdownProps,
  IHelpDropdownState
> {
  public onToggle: (isOpen: boolean) => void;
  public onSelect: () => void;

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
    this.onSelect = () => {
      this.setState({
        isOpen: !this.state.isOpen,
      });
    };
  }

  public render() {
    const { isOpen } = this.state;
    const {
      launchSampleIntegrationTutorials,
      launchUserGuide,
      launchConnectorsGuide,
      launchSupportPage,
      launchContactUs,
      launchAboutModal,
    } = this.props;
    const dropdownItems = [
      <DropdownItem
        data-testid={'help-dropdown-integration-tutorials-dropdown-item'}
        key="sampleIntegrationTutorials"
        component="a"
        onClick={launchSampleIntegrationTutorials}
      >
        Sample Integration Tutorials
      </DropdownItem>,
      <DropdownItem
        data-testid={'help-dropdown-user-guide-dropdown-item'}
        key="userGuide"
        component="a"
        onClick={launchUserGuide}
      >
        User Guide
      </DropdownItem>,
      <DropdownItem
        data-testid={'help-dropdown-connectors-guide-dropdown-item'}
        key="connectorsGuide"
        component="a"
        onClick={launchConnectorsGuide}
      >
        Connectors Guide
      </DropdownItem>,
      <DropdownItem
        data-testid={'help-dropdown-support-dropdown-item'}
        key="support"
        component="a"
        onClick={launchSupportPage}
      >
        Support
      </DropdownItem>,
      <DropdownItem
        data-testid={'help-dropdown-contact-us-dropdown-item'}
        key="contactUs"
        component="a"
        onClick={launchContactUs}
      >
        Contact Us
      </DropdownItem>,
      <DropdownItem
        data-testid={'help-dropdown-about-dropdown-item'}
        key="action"
        component="a"
        onClick={launchAboutModal}
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
              id="helpDropdownButton"
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
