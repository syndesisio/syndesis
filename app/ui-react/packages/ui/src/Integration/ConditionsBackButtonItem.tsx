import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink } from '../Layout';

import './ConditionsBackButtonItem.css';

export interface IConditionsBackButtonItemProps {
  title: string;
  href: H.LocationDescriptor;
}
export const ConditionsBackButtonItem: React.FunctionComponent<
  IConditionsBackButtonItemProps
> = ({ href, title }) => (
  <>
    <div className="conditions-back-button-item__back-button-wrapper">
      <ButtonLink
        data-testid="conditions-back-button-item-back-button"
        as="default"
        href={href}
      >
        {title}
      </ButtonLink>
    </div>
    <div className="pf-c-dropdown__separator" role="separator" />
  </>
);
