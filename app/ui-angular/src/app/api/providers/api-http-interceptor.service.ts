import { map } from 'rxjs/operators';
import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpInterceptor,
  HttpHandler,
  HttpRequest,
  HttpEventType
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { NotificationService } from '@syndesis/ui/common';
import { NotificationType } from 'patternfly-ng';

@Injectable()
export class ApiHttpInterceptor implements HttpInterceptor {
  constructor(private notificationService: NotificationService) {}

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      map((event: HttpEvent<any>) => {
        if (event.type === HttpEventType.Response) {
          if (
            event.body &&
            event.body['_meta'] &&
            event.body['_meta']['message']
          ) {
            const meta = event.body['_meta'];
            this.notificationService.popNotification({
              type: meta['type']
                ? (NotificationType[meta['type']] as string)
                : NotificationType.INFO,
              header: '',
              message: meta['message']
            });
          }
        }
        return event;
      })
    );
  }
}
