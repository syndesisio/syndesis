import * as H from '@syndesis/history';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { toValidHtmlId } from '../helpers';

import './ConditionsDropdownItem.css';

export interface IConditionsDropdownItemProps {
  name: string;
  description: string;
  condition: string;
  isCurrent: boolean;
  link: H.LocationDescriptor;
}
export const ConditionsDropdownItem: React.FunctionComponent<
  IConditionsDropdownItemProps
> = ({ condition, description, link, name, isCurrent }) => (
  <li>
    <Link
      data-testid={`conditions-dropdown-item-${toValidHtmlId(condition)}-link`}
      className="pf-c-dropdown__menu-item"
      to={link}
    >
      <div>
        <strong>{name}</strong>
      </div>
      <strong
        className="conditions-dropdown-item__condition"
        data-verb={condition}
      >
        {condition}
      </strong>
      &nbsp;{description}
      {isCurrent && (
        <i className="fa fa-check pull-right"/>
      )}
    </Link>
  </li>
);
