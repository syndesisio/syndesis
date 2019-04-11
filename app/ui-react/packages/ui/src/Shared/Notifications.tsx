import {
  TimedToastNotification,
  ToastNotificationList,
} from 'patternfly-react';
import * as React from 'react';
import { Container } from '../Layout/Container';

export type INotificationType = 'success' | 'info' | 'warning' | 'error';

export interface INotification {
  key: string;
  message: string;
  type: INotificationType;
}

export interface INotificationsProps {
  notifications: INotification[];
  notificationTimerDelay: number;
  removeNotificationAction(notification: INotification): void;
}

export class Notifications extends React.Component<INotificationsProps> {
  public render() {
    return (
      <ToastNotificationList className="app__notificationList">
        {this.props.notifications.map(notification => (
          <TimedToastNotification
            key={notification.key}
            type={notification.type}
            persistent={false}
            onDismiss={this.props.removeNotificationAction.bind(
              this,
              notification
            )}
            timerdelay={this.props.notificationTimerDelay}
          >
            <Container
              dangerouslySetInnerHTML={{
                __html: notification.message,
              }}
            />
          </TimedToastNotification>
        ))}
      </ToastNotificationList>
    );
  }
}
