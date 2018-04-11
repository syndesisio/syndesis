import { Injectable, ErrorHandler } from '@angular/core';

const GENERIC_ERROR_MSG = '[Syndesis] Exception logged';

@Injectable()
export class ExceptionHandlerService extends ErrorHandler {
  constructor() {
    super();
  }

  handleError(error: any): void {
    this.log(error);
    super.handleError(error);
  }

  log(error: any): void {
    /* tslint:disable no-console */
    if (console && console.error) {
      console.error(error || GENERIC_ERROR_MSG);
    }
  }
}
