import {
  fromEvent as observableFromEvent,
  merge as observableMerge,
  Observable,
  combineLatest
} from 'rxjs';

import { map } from 'rxjs/operators';
import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { NotificationType } from 'patternfly-ng';

import { NotificationService } from '@syndesis/ui/common';
import { I18NService } from '@syndesis/ui/platform';

@Injectable()
export class OfflineHandlerService {
  online$: Observable<boolean>;
  private errorMessages: {
    youAreOnline: string;
    youAreOffline: string;
    networkOnline: string;
    networkOffline: string;
  };

  constructor(
    @Inject(PLATFORM_ID) private platformId: any,
    private notificationService: NotificationService,
    private i18NService: I18NService
  ) {}

  initialize(enablePopNotifications: boolean): Promise<any> {
    return new Promise(resolve => {
      if (isPlatformBrowser(this.platformId)) {
        this.online$ = observableMerge(
          observableFromEvent(window, 'offline').pipe(map(() => false)),
          observableFromEvent(window, 'online').pipe(map(() => true))
        );
        // tslint:disable-next-line:deprecation
        combineLatest(
          this.i18NService.getValue('errors.youareonline'),
          this.i18NService.getValue('errors.youareoffline'),
          this.i18NService.getValue('errors.networkonline'),
          this.i18NService.getValue('errors.networkoffline')
        ).subscribe(
          ([youAreOnline, youAreOffline, networkOnline, networkOffline]) => {
            this.errorMessages = {
              youAreOnline,
              youAreOffline,
              networkOnline,
              networkOffline
            };
          }
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
        header: isOnline
          ? this.errorMessages.youAreOnline
          : this.errorMessages.youAreOffline,
        message: isOnline
          ? this.errorMessages.networkOnline
          : this.errorMessages.networkOffline
      });
    });
  }
}
