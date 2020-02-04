import { Text } from '@patternfly/react-core';
import * as React from 'react';

export interface IIntegrationEditorNothingToConfigureAlertProps {
  i18nAlert: string;
}

export const IntegrationEditorNothingToConfigureAlert: React.FunctionComponent<IIntegrationEditorNothingToConfigureAlertProps> = ({
  i18nAlert,
}) => (
  <Text className="alert alert-info">
    <span className="pficon pficon-info" />
    {i18nAlert}
  </Text>
);
