import { Breadcrumb as PfBreadcrumb } from 'patternfly-react';
import { ReactNodeArray } from 'react';
import * as React from 'react';

export interface IBreadcrumbProps {
  children: ReactNodeArray;
}

export const Breadcrumb: React.FunctionComponent<IBreadcrumbProps> = ({
  children,
}) => (
  <PfBreadcrumb>
    {React.Children.map(children, (c, idx) => (
      <li className={children.length - 1 === idx ? 'active' : ''} key={idx}>
        {c}
      </li>
    ))}
  </PfBreadcrumb>
);
