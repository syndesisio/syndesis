import { Injectable } from '@angular/core';

function _window(): Window {
  return window;
}

@Injectable()
export class WindowRef {
  get nativeWindow(): any {
    return _window();
  }
}
