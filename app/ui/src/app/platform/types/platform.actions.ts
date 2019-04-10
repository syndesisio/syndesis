import { Action } from '@ngrx/store';

export const APP_BOOTSTRAP = '[Platform] Main Application Bootstrap';

export class AppBootstrap implements Action {
  readonly type = APP_BOOTSTRAP;
}
