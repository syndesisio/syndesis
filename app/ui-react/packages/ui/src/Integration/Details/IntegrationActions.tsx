import {
  Dropdown,
  DropdownItem,
  DropdownPosition,
  KebabToggle
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
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

export const actionItem = (a: IIntegrationAction, idx: number, baseIdString: string) => {
  if (a.href) {
    return (
      <li role={'menuitem'} key={idx}>
        <Link to={a.href}
              data-testid={baseIdString + `-${toValidHtmlId(a.label.toString())}`}
              onClick={a.onClick}
              tabIndex={idx + 1}
              className={'pf-c-dropdown__menu-item'}
        >
          {a.label}
        </Link>
      </li>
    )
  } else {
    return (
      <DropdownItem key={idx}
                    data-testid={baseIdString + `integration-actions-${toValidHtmlId(a.label.toString())}`}
                    onClick={a.onClick}
                    tabIndex={idx + 1}
                    role={'menuitem'}
      >
        {a.label}
      </DropdownItem>
    );
  }
};

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

  const baseIdString = 'integration-actions';

  return (
    <>
      <ButtonLink
        data-testid={baseIdString + '-edit-button'}
        className={'edit-integration-btn'}
        href={editHref}
        as={'default'}
      >
        {i18nEditBtn}
      </ButtonLink>
      <ButtonLink
        data-testid={baseIdString + '-view-button'}
        className={'view-integration-btn'}
        href={detailsHref}
        as={'default'}
      >
        {i18nViewBtn}
      </ButtonLink>

      <Dropdown
        toggle={<KebabToggle onToggle={toggleDropdown} id="toggle-dropdown"/>}
        isOpen={showDropdown}
        isPlain={true}
        role={'presentation'}
        dropdownItems={actions.map((a, idx) => {
          return actionItem(a, idx, 'integration-actions')
        })}
        position={DropdownPosition.right}
        className={baseIdString + '__dropdown-kebab'}
        id={`integration-${integrationId}-action-menu`}
      />
    </>
  );
};
