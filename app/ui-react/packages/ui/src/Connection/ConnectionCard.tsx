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
import { InfoIcon, WarningTriangleIcon } from '@patternfly/react-icons';
import * as H from '@syndesis/history';
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

export const ConnectionCard: React.FunctionComponent<IConnectionProps> = ({
  description,
  href,
  i18nCannotDelete,
  i18nConfigRequired,
  i18nTechPreview,
  icon,
  isConfigRequired,
  isTechPreview,
  menuProps,
  name,
  techPreviewPopoverHtml,
}) => {
  const [showDeleteDialog, setShowDeleteDialog] = React.useState(false);
  const [isMenuOpen, setIsMenuOpen] = React.useState(false);
  const doDelete = () => {
    setShowDeleteDialog(false);
    if (menuProps) {
      menuProps.onDelete();
    }
  };
  return (
    <>
      {menuProps && (
        <ConfirmationDialog
          buttonStyle={ConfirmationButtonStyle.DANGER}
          i18nCancelButtonText={menuProps.i18nCancelLabel}
          i18nConfirmButtonText={menuProps.i18nDeleteLabel}
          i18nConfirmationMessage={menuProps.i18nDeleteModalMessage}
          i18nTitle={menuProps.i18nDeleteModalTitle}
          icon={ConfirmationIconType.DANGER}
          showDialog={showDeleteDialog}
          onCancel={() => setShowDeleteDialog(false)}
          onConfirm={doDelete}
        />
      )}
      <Card
        data-testid={`connection-card-${toValidHtmlId(name)}-card`}
        className={'connection-card'}
      >
        {isTechPreview && (
          <div
            className="connection-card__tech-preview"
            data-testid={'connection-card-tech-preview-heading'}
          >
            {i18nTechPreview!}
            {'  '}
            <Popover
              bodyContent={
                <React.Fragment>{techPreviewPopoverHtml}</React.Fragment>
              }
              aria-label={i18nTechPreview!}
              position={'left'}
            >
              <InfoIcon />
            </Popover>
          </div>
        )}
        {menuProps && (
          <div className="connection-card__dropdown">
            <Dropdown
              id={`connection-${name}-menu`}
              data-testid={'connection-card-dropdown'}
              onSelect={() => setIsMenuOpen(!isMenuOpen)}
              toggle={
                <KebabToggle
                  id="connection-card-kebab"
                  data-testid={'connection-card-kebab'}
                  onToggle={setIsMenuOpen}
                />
              }
              isOpen={isMenuOpen}
              isPlain={true}
              dropdownItems={[
                <PfDropdownItem key="view-action">
                  <Link
                    className="pf-c-dropdown__menu-item"
                    data-testid={'connection-card-view-action'}
                    to={href}
                    role={'menuitem'}
                    tabIndex={1}
                  >
                    {menuProps.i18nViewLabel}
                  </Link>
                </PfDropdownItem>,
                <PfDropdownItem key="edit-action">
                  <Link
                    className="pf-c-dropdown__menu-item"
                    data-testid={'connection-card-edit-action'}
                    to={menuProps.editHref}
                    role={'menuitem'}
                    tabIndex={2}
                  >
                    {menuProps.i18nEditLabel}
                  </Link>
                </PfDropdownItem>,
                <PfDropdownItem
                  disabled={!menuProps.isDeleteEnabled}
                  key="delete-action"
                  onClick={() => setShowDeleteDialog(true)}
                >
                  {!menuProps.isDeleteEnabled ? (
                    <Tooltip content={i18nCannotDelete!} position={'bottom'}>
                      <Button
                        className="pf-c-dropdown__menu-item"
                        isDisabled={true}
                        variant={'link'}
                      >
                        {menuProps.i18nDeleteLabel}
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
                      {menuProps.i18nDeleteLabel}
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
          to={href}
          className={'connection-card__content'}
        >
          <CardBody>
            <div className={'connection-card__body'}>
              <div className="connection-card__icon">{icon}</div>
              <Title
                headingLevel="h2"
                size="lg"
                className="connection-card__title h2"
                data-testid={'connection-card-title'}
              >
                {name}
              </Title>
              <Text
                className="connection-card__description"
                data-testid={'connection-card-description'}
              >
                {description}
              </Text>
            </div>
          </CardBody>
          {isConfigRequired && (
            <CardFooter
              className={
                'connection-card__footer--config-required alert alert-warning'
              }
              data-testid={'connection-card-config-required-footer'}
            >
              <WarningTriangleIcon />
              {i18nConfigRequired}
            </CardFooter>
          )}
        </Link>
      </Card>
    </>
  );
};
