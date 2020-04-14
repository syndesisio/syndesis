import * as React from 'react';
import { SyndesisAlert, SyndesisAlertLevel } from '../../Shared';

export interface IIntegrationEditorNothingToConfigureAlertProps {
  i18nAlert: string;
}

export const IntegrationEditorNothingToConfigureAlert: React.FunctionComponent<IIntegrationEditorNothingToConfigureAlertProps> = ({
  i18nAlert,
}) => <SyndesisAlert level={SyndesisAlertLevel.INFO} message={i18nAlert} />;
