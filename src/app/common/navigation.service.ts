import { Injectable } from '@angular/core';

@Injectable()
export class NavigationService {

  constructor() { }

  initialize() {
    $.fn.setupVerticalNavigation ? $.fn.setupVerticalNavigation() : '';
  }

  show() {
    $.fn.setupVerticalNavigation
      ? $.fn.setupVerticalNavigation().showMenu()
      : '';
  }

  hide() {
    $.fn.setupVerticalNavigation
      ? $.fn.setupVerticalNavigation().hideMenu()
      : '';

  }

}
