import * as React from 'react';

export interface IApiProviderIconProps {
  className?: string;
  alt?: string;
}

export const ApiProviderIcon: React.FunctionComponent<
  IApiProviderIconProps
> = props => (
  <img
    src={'/icons/api-provider.svg'}
    className={props.className}
    alt={props.alt}
  />
);
