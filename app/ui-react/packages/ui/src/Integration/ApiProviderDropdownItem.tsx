import * as H from '@syndesis/history';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { toValidHtmlId } from '../helpers';

export interface IApiProviderDropdownItemProps {
  children: React.ReactNode;
  flowName: string
  link: H.LocationDescriptor;
}
export const ApiProviderDropdownItem: React.FunctionComponent<IApiProviderDropdownItemProps> = ({ flowName, children, link })  => (
  <li>
    <Link
      data-testid={`api-provider-operations-dropdown-item-${toValidHtmlId(flowName)}-link`}
      className="pf-c-dropdown__menu-item"
      to={link}
    >
      {children}
      <div>
        <strong>{flowName}</strong>
      </div>
    </Link>
  </li>
);
