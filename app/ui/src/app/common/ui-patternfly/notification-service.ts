import { Injectable } from '@angular/core';
import { NotificationService as NS, Notification, Action } from 'patternfly-ng';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable()
export class NotificationService extends NS {
  notificationsSubject = new BehaviorSubject<Notification[]>(
    this.getNotifications()
  );

  remove(notification: Notification): void {
    super.remove(notification);
    this.notificationsSubject.next(this.getNotifications());
  }

  message(
    type: string,
    header: string,
    message: string,
    isPersistent: boolean,
    primaryAction?: Action,
    moreActions: Action[] = []
  ): void {
    super.message(
      type,
      header,
      message,
      isPersistent,
      primaryAction,
      moreActions
    );
    this.notificationsSubject.next(this.getNotifications());
  }

  getNotificationsObservable(): Observable<Notification[]> {
    return this.notificationsSubject.asObservable();
  }

  // Toast notification wrapper, used for notifications
  // that are not persistent and do not have actions associated
  popNotification({ type, header, message, isPersistent = false }): void {
    this.message(type, header, message, isPersistent);
  }
}
