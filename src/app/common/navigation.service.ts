import { Injectable } from '@angular/core';

@Injectable()
export class NavigationService {

  constructor() { }

  initialize() {
    (<any>$.fn).setupVerticalNavigation ? (<any>$.fn).setupVerticalNavigation() : '';
  }

  show() {
    (<any>$.fn).setupVerticalNavigation
      ? (<any>$.fn).setupVerticalNavigation().showMenu()
      : '';
  }

  hide() {
    (<any>$.fn).setupVerticalNavigation
      ? (<any>$.fn).setupVerticalNavigation().hideMenu()
      : '';

  }

}
