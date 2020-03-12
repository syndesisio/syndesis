import * as H from '@syndesis/history';
import { DropdownKebab } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { toValidHtmlId } from '../../helpers';
import { ButtonLink } from '../../Layout';
import './IntegrationActions.css';

export interface IIntegrationAction {
  href?: H.LocationDescriptor;
  onClick?: (e: React.MouseEvent<any>) => any;
  label: string | JSX.Element;
}

export interface IIntegrationActionsProps {
  i18nEditBtn: string;
  i18nViewBtn: string;
  integrationId: string;
  actions: IIntegrationAction[];
  detailsHref?: H.LocationDescriptor;
  editHref?: H.LocationDescriptor;
}

export const IntegrationActions: React.FunctionComponent<
  IIntegrationActionsProps
> = (
  {
    actions,
    detailsHref,
    editHref,
    i18nEditBtn,
    i18nViewBtn,
    integrationId
  }) => {
  return (
    <>
      <ButtonLink
        data-testid={'integration-actions-edit-button'}
        className={'edit-integration-btn'}
        href={editHref}
        as={'default'}
      >
        {i18nEditBtn}
      </ButtonLink>
      <ButtonLink
        data-testid={'integration-actions-view-button'}
        className={'view-integration-btn'}
        href={detailsHref}
        as={'default'}
      >
        {i18nViewBtn}
      </ButtonLink>
      <DropdownKebab
        className={'integration-actions__dropdown-kebab'}
        id={`integration-${integrationId}-action-menu`}
        pullRight={true}
      >
        {actions.map((a, idx) => (
          <li role={'presentation'} key={idx}>
            {a.href ? (
              <Link
                data-testid={`integration-actions-${toValidHtmlId(
                  a.label.toString()
                )}`}
                to={a.href}
                onClick={a.onClick}
                role={'menuitem'}
                tabIndex={idx + 1}
              >
                {a.label}
              </Link>
            ) : (
              <a
                data-testid={`integration-actions-${toValidHtmlId(
                  a.label.toString()
                )}`}
                href={'javascript:void(0)'}
                onClick={a.onClick}
                role={'menuitem'}
                tabIndex={idx + 1}
              >
                {a.label}
              </a>
            )}
          </li>
        ))}
      </DropdownKebab>
    </>
  );
};
