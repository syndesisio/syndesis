import { Breadcrumb as PfBreadcrumb } from 'patternfly-react';
import * as React from 'react';

export interface IBreadcrumbProps {
  children: React.ReactNode;
}

/**
 * A component to show breadcrumbs. All its children will be wrapped in a tag
 * that will automatically handle the active/inactive state by setting the
 * appropriate class to the wrapper.
 *
 * It's suggested to use only anchors or spans as children node.
 */
export const Breadcrumb: React.FunctionComponent<IBreadcrumbProps> = ({
  children,
}) => (
  <PfBreadcrumb
    style={{
      background: '#fff',
      margin: 0,
      paddingLeft: '15px',
    }}
  >
    {React.Children.map(children, (c, idx) => (
      <li
        className={React.Children.count(children) - 1 === idx ? 'active' : ''}
        key={idx}
      >
        {c}
      </li>
    ))}
  </PfBreadcrumb>
);
