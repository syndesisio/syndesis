import { APP_INITIALIZER, ErrorHandler } from '@angular/core';
import { HTTP_INTERCEPTORS } from '@angular/common/http';

import { ExceptionHandlerService } from '@syndesis/ui/error-handler/exception-handler.service';
import { HttpErrorInterceptor } from '@syndesis/ui/error-handler/http-error.interceptor';
import { OfflineHandlerService } from '@syndesis/ui/error-handler/offline-handler.service';

export function offlineHandlerInitializer(
  offlineHandlerService: OfflineHandlerService
): () => Promise<OfflineHandlerService> {
  return () => offlineHandlerService.initialize(true);
}

export const ERROR_HANDLER_PROVIDERS = [
  {
    provide: ErrorHandler,
    useClass: ExceptionHandlerService
  },
  OfflineHandlerService,
  {
    provide: APP_INITIALIZER,
    useFactory: offlineHandlerInitializer,
    deps: [OfflineHandlerService],
    multi: true
  },
  {
    provide: HTTP_INTERCEPTORS,
    useClass: HttpErrorInterceptor,
    multi: true
  }
];
