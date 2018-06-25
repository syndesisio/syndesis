import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { combineLatest } from 'rxjs/observable/combineLatest';
import { NotificationType } from 'patternfly-ng';

import { NotificationService } from '@syndesis/ui/common';
import { I18NService } from '@syndesis/ui/platform';

const GENERIC_HTTP_ERROR_MSG = '[Syndesis] HTTP Exception logged';

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {
  private errorMessages: {
    httpError: string;
    httpErrorMessage: string;
  };

  constructor(private notificationService: NotificationService, private i18NService: I18NService) {
    combineLatest(
      this.i18NService.getValue('errors.httperror'),
      this.i18NService.getValue('errors.httperrordefaultmsg')
    ).subscribe(([httpError, httpErrorMessage]) => {
      this.errorMessages = { httpError, httpErrorMessage };
    });
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).do((event: HttpEvent<any>) => event, (error: any) => {
      if (error instanceof HttpErrorResponse) {
        /* tslint:disable no-console */
        if (console && console.error) {
          console.error('Error performing ' + request.method + ' request to ' + request.url + ' : ', error || GENERIC_HTTP_ERROR_MSG);
        }
      }
    });
  }
}
