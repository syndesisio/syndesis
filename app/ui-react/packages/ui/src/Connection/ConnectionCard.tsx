import {
  Button,
  Card,
  CardBody,
  CardFooter,
  Dropdown,
  DropdownPosition,
  KebabToggle,
  Popover,
  Text,
  Title,
  Tooltip,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import { Icon } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { toValidHtmlId } from '../helpers';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
  PfDropdownItem,
} from '../Shared';
import './ConnectionCard.css';

export interface IConnectionCardMenuProps {
  editHref: H.LocationDescriptor;
  i18nCancelLabel: string;
  i18nDeleteLabel: string;
  i18nDeleteModalMessage: string;
  i18nDeleteModalTitle: string;
  i18nEditLabel: string;
  i18nMenuTitle: string;
  i18nViewLabel: string;
  isDeleteEnabled: boolean;

  onDelete(): void;
}

export interface IConnectionProps {
  description: string;
  href: H.LocationDescriptor;
  i18nCannotDelete?: string;
  i18nConfigRequired?: string;
  i18nTechPreview?: string;
  icon: React.ReactNode;
  isConfigRequired?: boolean;
  isTechPreview?: boolean;
  menuProps?: IConnectionCardMenuProps;
  name: string;
  techPreviewPopoverHtml?: React.ReactNode;
}

export interface IConnectionCardState {
  isMenuOpen: boolean;
  showDeleteDialog: boolean;
}

export class ConnectionCard extends React.PureComponent<
  IConnectionProps,
  IConnectionCardState
> {
  public constructor(props: IConnectionProps) {
    super(props);

    this.state = {
      isMenuOpen: false,
      showDeleteDialog: false, // initial visibility of delete dialog
    };

    if (this.props.menuProps) {
      this.doCancel = this.doCancel.bind(this);
      this.doDelete = this.doDelete.bind(this);
      this.showDeleteDialog = this.showDeleteDialog.bind(this);
    }
  }

  public doCancel() {
    this.setState({
      showDeleteDialog: false, // hide dialog
    });
  }

  public doDelete() {
    this.setState({
      showDeleteDialog: false, // hide dialog
    });

    if (this.props.menuProps) {
      this.props.menuProps.onDelete();
    }
  }
  public onToggle = (isMenuOpen: boolean) => {
    this.setState({
      isMenuOpen,
    });
  };

  public onMenuSelect = (
    event: React.SyntheticEvent<HTMLDivElement, Event>
  ) => {
    this.setState({
      isMenuOpen: !this.state.isMenuOpen,
    });
  };

  public showDeleteDialog() {
    this.setState({
      showDeleteDialog: true,
    });
  }

  public render() {
    return (
      <>
        {this.props.menuProps && (
          <ConfirmationDialog
            buttonStyle={ConfirmationButtonStyle.DANGER}
            i18nCancelButtonText={this.props.menuProps.i18nCancelLabel}
            i18nConfirmButtonText={this.props.menuProps.i18nDeleteLabel}
            i18nConfirmationMessage={
              this.props.menuProps.i18nDeleteModalMessage
            }
            i18nTitle={this.props.menuProps.i18nDeleteModalTitle}
            icon={ConfirmationIconType.DANGER}
            showDialog={this.state.showDeleteDialog}
            onCancel={this.doCancel}
            onConfirm={this.doDelete}
          />
        )}
        <Card
          data-testid={`connection-card-${toValidHtmlId(this.props.name)}-card`}
          className={'connection-card'}
        >
          {this.props.isTechPreview && (
            <div
              className="connection-card__tech-preview"
              data-testid={'connection-card-tech-preview-heading'}
            >
              {this.props.i18nTechPreview!}
              {'  '}
              <Popover
                bodyContent={
                  <React.Fragment>
                    {this.props.techPreviewPopoverHtml}
                  </React.Fragment>
                }
                aria-label={this.props.i18nTechPreview!}
                position={'left'}
              >
                <Icon type={'pf'} name={'info'} />
              </Popover>
            </div>
          )}
          {this.props.menuProps && (
            <div className="connection-card__dropdown">
              <Dropdown
                id={`connection-${this.props.name}-menu`}
                data-testid={'connection-card-dropdown'}
                onSelect={this.onMenuSelect}
                toggle={
                  <KebabToggle
                    id="connection-card-kebab"
                    data-testid={'connection-card-kebab'}
                    onToggle={this.onToggle}
                  />
                }
                isOpen={this.state.isMenuOpen}
                isPlain={true}
                dropdownItems={[
                  <PfDropdownItem key="view-action">
                    <Link
                      className="pf-c-dropdown__menu-item"
                      data-testid={'connection-card-view-action'}
                      to={this.props.href}
                      role={'menuitem'}
                      tabIndex={1}
                    >
                      {this.props.menuProps.i18nViewLabel}
                    </Link>
                  </PfDropdownItem>,
                  <PfDropdownItem key="edit-action">
                    <Link
                      className="pf-c-dropdown__menu-item"
                      data-testid={'connection-card-edit-action'}
                      to={this.props.menuProps.editHref}
                      role={'menuitem'}
                      tabIndex={2}
                    >
                      {this.props.menuProps.i18nEditLabel}
                    </Link>
                  </PfDropdownItem>,
                  <PfDropdownItem
                    disabled={!this.props.menuProps.isDeleteEnabled}
                    key="delete-action"
                    onClick={this.showDeleteDialog}
                  >
                    {!this.props.menuProps.isDeleteEnabled ? (
                      <Tooltip
                        content={this.props.i18nCannotDelete!}
                        position={'bottom'}
                      >
                        <Button
                          className="pf-c-dropdown__menu-item"
                          isDisabled={true}
                          variant={'link'}
                        >
                          {this.props.menuProps.i18nDeleteLabel}
                        </Button>
                      </Tooltip>
                    ) : (
                      <a
                        className="pf-c-dropdown__menu-item"
                        data-testid={'connection-card-delete-action'}
                        href={'javascript:void(0)'}
                        role={'menuitem'}
                        tabIndex={3}
                      >
                        {this.props.menuProps.i18nDeleteLabel}
                      </a>
                    )}
                  </PfDropdownItem>,
                ]}
                position={DropdownPosition.right}
              />
            </div>
          )}
          <Link
            data-testid={'connection-card-details-link'}
            to={this.props.href}
            className={'connection-card__content'}
          >
            <CardBody>
              <div className={'connection-card__body'}>
                <div className="connection-card__icon">{this.props.icon}</div>
                <Title
                  headingLevel="h2"
                  size="lg"
                  className="connection-card__title h2"
                  data-testid={'connection-card-title'}
                >
                  {this.props.name}
                </Title>
                <Text
                  className="connection-card__description"
                  data-testid={'connection-card-description'}
                >
                  {this.props.description}
                </Text>
              </div>
            </CardBody>
            {this.props.isConfigRequired && (
              <CardFooter
                className={
                  'connection-card__footer--config-required alert alert-warning'
                }
                data-testid={'connection-card-config-required-footer'}
              >
                <Icon type={'pf'} name={'warning-triangle-o'} size={'2x'} />
                {this.props.i18nConfigRequired}
              </CardFooter>
            )}
          </Link>
        </Card>
      </>
    );
  }
}
