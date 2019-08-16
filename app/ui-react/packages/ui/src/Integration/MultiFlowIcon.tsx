import * as React from 'react';

export interface IMultiFlowIconProps {
  className?: string;
  alt?: string;
}

export const MultiFlowIcon: React.FunctionComponent<
  IMultiFlowIconProps
> = props => (
  <img
    src={'/icons/multi-flow.integration.png'}
    className={props.className}
    alt={props.alt}
  />
);
