import { tap } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, combineLatest } from 'rxjs';
import { NotificationType } from 'patternfly-ng';

import { NotificationService } from '@syndesis/ui/common';
import { I18NService } from '@syndesis/ui/platform';

const GENERIC_HTTP_ERROR_MSG = '[Syndesis] HTTP Exception logged';

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {
  private errorMessages: {
    httpConnectFailureHeader: string;
    httpConnectFailureMessage: string;
  };

  constructor(
    private notificationService: NotificationService,
    private i18NService: I18NService
  ) {
    // tslint:disable-next-line:deprecation
    combineLatest(
      this.i18NService.getValue('errors.httpconnectfailurehdr'),
      this.i18NService.getValue('errors.httpconnectfailuremsg')
    ).subscribe(([httpConnectFailureHeader, httpConnectFailureMessage]) => {
      this.errorMessages = {
        httpConnectFailureHeader,
        httpConnectFailureMessage
      };
    });
  }

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      tap(
        (event: HttpEvent<any>) => event,
        (error: any) => {
          if (error instanceof HttpErrorResponse) {
            /* tslint:disable no-console */
            if (console && console.error) {
              console.error(
                'Error performing ' +
                  request.method +
                  ' request to ' +
                  request.url +
                  ' : ',
                error || GENERIC_HTTP_ERROR_MSG
              );
            }
            switch (error.status) {
              case 0:
              case 504:
              case 503:
              case 502:
                if (
                  this.notificationService
                    .getNotifications()
                    .find(
                      n =>
                        n.header === this.errorMessages.httpConnectFailureHeader
                    )
                ) {
                  // Only show one message
                  return;
                }
                this.notificationService.popNotification(<any>{
                  type: NotificationType.DANGER,
                  header: this.errorMessages.httpConnectFailureHeader,
                  message: this.errorMessages.httpConnectFailureMessage,
                  isPersistent: true
                });
                break;
              default:
            }
          }
        }
      )
    );
  }
}
