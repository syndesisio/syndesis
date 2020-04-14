import { Alert, AlertProps } from '@patternfly/react-core';
import * as React from 'react';

export interface ITimedAlertProps extends AlertProps {
  persistent: boolean;
  timeout: number;
  onClose: () => void;
}

export const TimedAlert: React.FunctionComponent<ITimedAlertProps> = ({
  persistent,
  timeout,
  onClose,
  ...rest
}) => {
  if (!persistent) {
    setTimeout(onClose, timeout);
  }
  return <Alert {...rest} />;
};
