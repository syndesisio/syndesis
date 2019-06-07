import * as React from 'react';

import './ConditionsDropdownItem.css';

export interface IConditionsDropdownBodyProps {
  description: string;
  condition: string;
}
export const ConditionsDropdownBody: React.FunctionComponent<
  IConditionsDropdownBodyProps
> = ({ condition, description }) => (
  <>
    <strong
      className="conditions-dropdown-item__condition"
      data-verb={condition}
    >
      {condition}
    </strong>
    &nbsp;{description}
  </>
);
