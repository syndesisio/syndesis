import {
  Dropdown,
  DropdownPosition,
  KebabToggle,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { toValidHtmlId } from '../../helpers';
import { Breadcrumb, ButtonLink } from '../../Layout';
import { IMenuActions, PfDropdownItem } from '../../Shared';
import './IntegrationDetailBreadcrumb.css';

export interface IIntegrationDetailBreadcrumbProps {
  editHref?: H.LocationDescriptor;
  editLabel?: string | JSX.Element;
  exportAction?: (e: React.MouseEvent<any>) => any;
  exportHref?: H.LocationDescriptor;
  exportLabel?: string | JSX.Element;
  homeHref?: H.LocationDescriptor;
  i18nHome?: string;
  i18nIntegrations?: string;
  i18nPageTitle?: string;
  integrationsHref?: H.LocationDescriptor;
  menuActions?: IMenuActions[];
}

export const IntegrationDetailBreadcrumb: React.FunctionComponent<IIntegrationDetailBreadcrumbProps> = ({
  editHref,
  editLabel,
  exportAction,
  exportHref,
  exportLabel,
  homeHref,
  i18nHome,
  i18nIntegrations,
  i18nPageTitle,
  integrationsHref,
  menuActions,
}) => {
  const [isMenuOpen, setIsMenuOpen] = React.useState(false);
  return (
    <Breadcrumb
      actions={
        <>
          <ButtonLink
            data-testid={'integration-detail-breadcrumb-export-button'}
            to={exportHref}
            onClick={exportAction}
            children={exportLabel}
          />
          &nbsp;&nbsp;
          <ButtonLink
            data-testid={'integration-detail-breadcrumb-edit-button'}
            as={'primary'}
            href={editHref}
            children={editLabel}
          />
          {menuActions && (
            <Dropdown
              data-testid={'integration-detail-breadcrumb-dropdown'}
              toggle={
                <KebabToggle
                  data-testid={'integration-detail-breadcrumb-kebab-toggle'}
                  onToggle={setIsMenuOpen}
                />
              }
              isOpen={isMenuOpen}
              isPlain={true}
              position={DropdownPosition.right}
              dropdownItems={menuActions!.map((menuAction, index) => (
                <PfDropdownItem
                  key={`dropdown-item-${index}`}
                  onClick={event => {
                    setIsMenuOpen(false);
                    if (menuAction && menuAction.onClick) {
                      menuAction.onClick(event as any);
                    }
                  }}
                >
                  {menuAction.href ? (
                    <Link
                      data-testid={`integration-detail-breadcrumb-${toValidHtmlId(
                        menuAction.label.toString()
                      )}`}
                      className="pf-c-dropdown__menu-item"
                      to={menuAction.href}
                      role={'menuitem'}
                      tabIndex={index + 1}
                    >
                      {menuAction.label}
                    </Link>
                  ) : (
                    <a
                      data-testid={`integration-detail-breadcrumb-${toValidHtmlId(
                        menuAction.label.toString()
                      )}`}
                      className="pf-c-dropdown__menu-item"
                      href={'javascript:void(0)'}
                      role={'menuitem'}
                      tabIndex={index + 1}
                    >
                      {menuAction.label}
                    </a>
                  )}
                </PfDropdownItem>
              ))}
            />
          )}
        </>
      }
    >
      <span>
        <Link
          data-testid={'integration-detail-breadcrumb-home-link'}
          to={homeHref!}
        >
          {i18nHome}
        </Link>
      </span>
      <span>
        <Link
          data-testid={'integration-detail-breadcrumb-integrations-link'}
          to={integrationsHref!}
        >
          {i18nIntegrations}
        </Link>
      </span>
      <span>{i18nPageTitle}</span>
    </Breadcrumb>
  );
};
