import * as H from '@syndesis/history';
import {
  Dropdown,
  DropdownItem,
  DropdownPosition,
  KebabToggle
} from '@patternfly/react-core';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { toValidHtmlId } from '../../helpers';
import { ButtonLink } from '../../Layout';
import './IntegrationActions.css';

export interface IIntegrationAction {
  href?: H.LocationDescriptor;
  onClick?: (event: React.MouseEvent<any> | React.KeyboardEvent | MouseEvent) => void;
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

export const IntegrationActions: React.FunctionComponent<IIntegrationActionsProps> = (
  {
    actions,
    detailsHref,
    editHref,
    i18nEditBtn,
    i18nViewBtn,
    integrationId
  }) => {
  const [showDropdown, setShowDropdown] = React.useState(false);

  const toggleDropdown = () => {
    setShowDropdown(!showDropdown);
  };

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

      <Dropdown
        toggle={<KebabToggle onToggle={toggleDropdown} id="toggle-dropdown"/>}
        isOpen={showDropdown}
        isPlain
        role={'presentation'}
        dropdownItems={actions.map((a, idx) => (
          <DropdownItem key={idx}
                        data-testid={`integration-actions-${toValidHtmlId(a.label.toString())}`}
                        onClick={a.onClick}
                        tabIndex={idx + 1}
                        role={'menuitem'}
          >
            {a.href ? (
              <Link to={a.href}>
                {a.label}
              </Link>
            ) : a.label}
          </DropdownItem>
        ))}
        position={DropdownPosition.right}
        className={'integration-actions__dropdown-kebab'}
        id={`integration-${integrationId}-action-menu`}
      />
    </>
  );
};
