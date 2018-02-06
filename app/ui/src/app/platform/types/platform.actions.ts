import { Action } from '@ngrx/store';

export const PLATFORM_BOOTSTRAP = '[Platform] Main Application Bootstrap';

export class PlatformBootstrap implements Action {
  readonly type = PLATFORM_BOOTSTRAP;
}
