import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { NotificationType } from 'patternfly-ng';

import { NotificationService } from '@syndesis/ui/common';

const GENERIC_HTTP_ERROR_MSG = '[Syndesis] HTTP Exception logged';

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {
  constructor(private notificationService: NotificationService) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).do((event: HttpEvent<any>) => event, (error: any) => {
      if (error instanceof HttpErrorResponse) {
        this.notificationService.popNotification({
          type: NotificationType.DANGER,
          header: 'The server returned an error',
          message: error && error.message ? error.message : 'Your request could not be completed. Please try again.'
        });

        /* tslint:disable no-console */
        if (console && console.error) {
          console.error(error || GENERIC_HTTP_ERROR_MSG);
        }
      }
    });
  }
}
