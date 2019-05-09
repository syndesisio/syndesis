import * as React from 'react';
import { useIntegrationHelpers } from './useIntegrationHelpers';

export interface IWithIntegrationHelpersProps {
  children(props: ReturnType<typeof useIntegrationHelpers>): any;
}

/**
 * This component provides through a render prop a number of helper
 * functions useful when working with an integration.
 *
 * Some of these helpers are available also as stand-alone functions
 * (packages/api/src/integrationHelpers/index.ts), but this component provides
 * methods like `saveIntegration` that can talk with the backend without any
 * additional information provided.
 *
 * Methods that modify an integration return a immutable copy of the original
 * object, to reduce the risk of bugs.
 */
export const WithIntegrationHelpers: React.FunctionComponent<
  IWithIntegrationHelpersProps
> = ({ children }) => {
  const helpers = useIntegrationHelpers();

  return children(helpers);
};
