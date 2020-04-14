import { Spinner } from '@patternfly/react-core';
import classnames from 'classnames';
import * as React from 'react';

import './Loader.css';

export interface ILoaderProps {
  inline?: boolean;
  size?: 'lg' | 'md' | 'sm' | 'xs';
}

export const Loader: React.FunctionComponent<ILoaderProps> = ({
  inline,
  size,
}) => {
  const isInline = typeof inline === 'undefined' ? false : inline;
  let mappedSize: 'xl' | 'lg' | 'md' | 'sm' | undefined = 'xl';
  switch (size) {
    case 'lg':
      mappedSize = 'xl';
      break;
    case 'md':
      mappedSize = 'lg';
      break;
    case 'sm':
      mappedSize = 'md';
      break;
    case 'xs':
      mappedSize = 'sm';
      break;
    default:
  }
  return (
    <div
      className={classnames('Loader', {
        'is-block': !isInline,
        'is-inline': isInline,
      })}
    >
      <Spinner
        size={mappedSize}
      />
    </div>
  );
};
