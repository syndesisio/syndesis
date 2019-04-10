import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpInterceptor,
  HttpHandler,
  HttpRequest,
  HttpXsrfTokenExtractor
} from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from 'environments/environment';

@Injectable()
export class ApiXsrfInterceptor implements HttpInterceptor {
  constructor(private tokenExtractor: HttpXsrfTokenExtractor) {}

  intercept(
    httpRequest: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    if (httpRequest.url.startsWith('http')) {
      const token =
        this.tokenExtractor.getToken() || environment.xsrf.defaultTokenValue;
      const { headerName } = environment.xsrf;

      if (!httpRequest.headers.has(headerName)) {
        httpRequest = httpRequest.clone({
          headers: httpRequest.headers.set(headerName, token)
        });
      }
    }

    return next.handle(httpRequest);
  }
}
