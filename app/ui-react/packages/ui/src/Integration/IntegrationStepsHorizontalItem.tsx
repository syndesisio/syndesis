import { AngleRightIcon } from '@patternfly/react-icons';
import * as H from '@syndesis/history';
import * as React from 'react';
import { Link } from 'react-router-dom';

import './IntegrationStepsHorizontalItem.css';

export interface IIntegrationStepsHorizontalItemProps {
  /**
   * The name of the connector used for the step.
   */
  name?: H.LocationDescriptor;
  /**
   * The icon of the step.
   */
  icon: React.ReactNode;
  title?: string;
  href?: string;
  /**
   * The boolean value that determines if the step
   * is the first in the steps array.
   */
  isLast?: boolean;
}

export const IntegrationStepsHorizontalItem: React.FunctionComponent<IIntegrationStepsHorizontalItemProps> = ({
  name,
  icon,
  title,
  href,
  isLast,
}) => (
  <div className="integration-steps-horizontal-item">
    {!href && (
      <div>
        <div className={'step-icon'} title={title}>
          {icon}
        </div>
        <p>{name}</p>
      </div>
    )}
    {href && (
      <Link to={href}>
        <div>
          <div className={'step-icon'} title={title}>
            {icon}
          </div>
          <p>{name}</p>
        </div>
      </Link>
    )}
    {isLast === false ? <AngleRightIcon className="step-arrow" /> : null}
  </div>
);
