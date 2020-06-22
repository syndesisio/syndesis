import * as React from 'react';

export interface IApiConnectorDetailConfig {
  /**
   * The localized text for the edit button.
   */
  i18nEditLabel?: string;

  // Initial properties
  properties?: any;
}

export const ApiConnectorDetailConfig: React.FunctionComponent<IApiConnectorDetailConfig> = ({
  i18nEditLabel,
}) => {
  // tslint:disable:no-console

  return (
    <>
      <p>Not editing..</p>
    </>
  );
};
