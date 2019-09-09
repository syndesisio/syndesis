import {
  Dropdown,
  DropdownDirection,
  DropdownItem,
  DropdownPosition,
  DropdownToggle,
  KebabToggle,
} from '@patternfly/react-core';
import { HelpIcon } from '@patternfly/react-icons';
import classNames from 'classnames';
import * as React from 'react';

export interface IHelpDropdownProps {
  additionalDropdownItems?: React.ReactNode[];
  className?: string;
  isTabletView: boolean;
  isOpen: boolean;
  dropdownDirection?: keyof typeof DropdownDirection;
  dropdownPosition?: keyof typeof DropdownPosition;
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
      additionalDropdownItems = [],
      dropdownDirection,
      dropdownPosition,
      launchSampleIntegrationTutorials,
      launchUserGuide,
      launchConnectorsGuide,
      launchSupportPage,
      launchContactUs,
      launchAboutModal,
      isTabletView,
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
        onClick={ev => {
          ev.preventDefault();
          launchAboutModal();
        }}
      >
        About
      </DropdownItem>,
    ];
    const dropdownId = 'helpDropdownButton';
    return (
      <>
        <Dropdown
          direction={dropdownDirection || DropdownDirection.down}
          position={dropdownPosition || DropdownPosition.right}
          onSelect={this.onSelect}
          toggle={
            isTabletView ? (
              <KebabToggle
                id={dropdownId}
                aria-label="Global dropdown"
                data-testid={dropdownId}
                className={classNames('', this.props.className)}
                onToggle={this.onToggle}
              />
            ) : (
              <DropdownToggle
                id={dropdownId}
                aria-label="Help dropdown"
                data-testid={dropdownId}
                className={classNames('', this.props.className)}
                onToggle={this.onToggle}
                iconComponent={null}
              >
                <HelpIcon />
              </DropdownToggle>
            )
          }
          isOpen={isOpen}
          isPlain={true}
          dropdownItems={[...dropdownItems, ...additionalDropdownItems]}
        />
      </>
    );
  }
}
