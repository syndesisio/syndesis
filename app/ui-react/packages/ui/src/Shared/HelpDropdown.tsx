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
        key="sampleIntegrationTutorials"
        component="span"
        onClick={launchSampleIntegrationTutorials}
      >
        Sample Integration Tutorials
      </DropdownItem>,
      <DropdownItem key="userGuide" component="span" onClick={launchUserGuide}>
        User Guide
      </DropdownItem>,
      <DropdownItem
        key="connectorsGuide"
        component="span"
        onClick={launchConnectorsGuide}
      >
        Connectors Guide
      </DropdownItem>,
      <DropdownItem key="support" component="span" onClick={launchSupportPage}>
        Support
      </DropdownItem>,
      <DropdownItem key="contactUs" component="span" onClick={launchContactUs}>
        Contact Us
      </DropdownItem>,
      <DropdownItem key="action" component="span" onClick={launchAboutModal}>
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
