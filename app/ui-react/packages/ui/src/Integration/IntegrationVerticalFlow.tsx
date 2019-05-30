import classnames from 'classnames';
import * as React from 'react';
import { ButtonLink } from '../Layout';
import './IntegrationVerticalFlow.css';

export interface IIntegrationVerticalFlowProps {
  /**
   * A render prop that receives the expanded state of the container.
   */
  children: (props: { expanded: boolean }) => any;
  initialExpanded?: boolean;
}

/**
 * A component to render an expandable container meant to be used to show an
 * integration's steps.
 * @see [children]{@link IIntegrationVerticalFlowProps#children}
 */
export const IntegrationVerticalFlow: React.FunctionComponent<
  IIntegrationVerticalFlowProps
> = ({ children, initialExpanded = true }) => {
  const [expanded, setExpanded] = React.useState(
    initialExpanded !== undefined
      ? initialExpanded
      : (localStorage.getItem('iec-vertical-flow-expanded') || 'y') === 'y'
  );

  const toggleExpanded = () => {
    localStorage.setItem('iec-vertical-flow-expanded', !expanded ? 'y' : 'n');
    setExpanded(!expanded);
  };

  return (
    <div
      className={classnames('integration-vertical-flow', {
        'is-expanded': expanded,
      })}
    >
      <div className="integration-vertical-flow__expand">
        <ButtonLink
          data-testid={'integration-vertical-flow-expand-collapse-button'}
          className="integration-vertical-flow__toggle"
          onClick={toggleExpanded}
          as={'link'}
        >
          {expanded === false ? (
            <>
              Expand{' '}
              <i className="fa fa-angle-double-right integration-vertical-flow__icon" />
            </>
          ) : (
            <>
              Collapse{' '}
              <i className="fa fa-angle-double-left integration-vertical-flow__icon" />
            </>
          )}
        </ButtonLink>
      </div>
      <div className="integration-vertical-flow__body">
        {children({ expanded })}
      </div>
    </div>
  );
};
