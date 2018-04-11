import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Observable } from 'rxjs/Observable';
import { NotificationType } from 'patternfly-ng';

import { NotificationService } from '@syndesis/ui/common';

@Injectable()
export class OfflineHandlerService {
  online$: Observable<boolean>;

  constructor(
    @Inject(PLATFORM_ID) private platformId: any,
    private notificationService: NotificationService
  ) { }

  initialize(enablePopNotifications: boolean): Promise<any> {
    return new Promise(resolve => {
      if (isPlatformBrowser(this.platformId)) {
        this.online$ = Observable.merge(
          Observable.fromEvent(window, 'offline').map(() => false),
          Observable.fromEvent(window, 'online').map(() => true),
        );

        if (enablePopNotifications) {
          this.initializePopNotifications();
        }
      }

      return resolve();
    });
  }

  private initializePopNotifications(): void {
    this.online$.subscribe(isOnline => {
      this.notificationService.popNotification({
        type: isOnline ? NotificationType.SUCCESS : NotificationType.DANGER,
        header: isOnline ? 'You\'re back online' : 'You went offline',
        message: isOnline ? 'Your internet connection has been restored' : 'Your internet connection has been interrupted'
      });
    });
  }
}
