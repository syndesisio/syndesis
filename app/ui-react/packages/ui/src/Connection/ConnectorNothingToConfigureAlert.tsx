import * as React from 'react';
import { SyndesisAlert, SyndesisAlertLevel } from '../Shared';

export interface IConnectorNothingToConfigureAlertProps {
  i18nAlert: string;
}

export const ConnectorNothingToConfigureAlert: React.FunctionComponent<IConnectorNothingToConfigureAlertProps> = ({
  i18nAlert,
}) => <SyndesisAlert level={SyndesisAlertLevel.INFO} message={i18nAlert} />;
