import * as React from 'react';
import { useConnectionHelpers } from './useConnectionHelpers';

export interface IWithConnectionHelpersProps {
  children(props: ReturnType<typeof useConnectionHelpers>): any;
}

export const WithConnectionHelpers: React.FunctionComponent<
  IWithConnectionHelpersProps
> = ({ children }) => {
  const helpers = useConnectionHelpers();

  return children(helpers);
};
