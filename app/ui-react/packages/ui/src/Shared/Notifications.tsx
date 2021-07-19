import { AlertActionCloseButton, AlertGroup } from '@patternfly/react-core';
import * as React from 'react';
import { TimedAlert } from './TimedAlert';

export type INotificationType = 'success' | 'info' | 'warning' | 'error';

export interface INotification {
  key: string;
  message: React.ReactNode;
  persistent: boolean;
  type: INotificationType;
}

export interface INotificationsProps {
  notifications: INotification[];
  onClose(key: string): void;
}

function mapType(incoming: INotificationType) {
  switch (incoming) {
    case 'error':
      return 'danger';
    default:
      return incoming;
  }
}

export const Notifications: React.FunctionComponent<INotificationsProps> = ({
  notifications,
  onClose,
}) => {
  return (
    <AlertGroup className={'app__notificationList'} isToast={true}>
      {notifications.map(({ key, type, message, persistent }) => (
        <TimedAlert
          key={key}
          timeout={8000}
          persistent={persistent}
          onClose={() => onClose(key)}
          variant={mapType(type)}
          title={message}
          isLiveRegion={true}
          actionClose={<AlertActionCloseButton onClose={() => onClose(key)} />}
        />
      ))}
    </AlertGroup>
  );
};
