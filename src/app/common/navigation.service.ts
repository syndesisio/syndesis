import { Injectable } from '@angular/core';

@Injectable()
export class NavigationService {
  private jQuery: JQuery;
  private verticalNavigationHandler: any;

  constructor() {
    this.jQuery = $.fn;
  }

  initialize() {
    if (this.setupVerticalNavigation) {
      this.verticalNavigationHandler = this.setupVerticalNavigation();
    }
  }

  show() {
    if (this.verticalNavigationHandler) {
      this.verticalNavigationHandler.showMenu();
    }
  }

  hide() {
    if (this.verticalNavigationHandler) {
      this.verticalNavigationHandler.hideMenu();
    }
  }

  private get setupVerticalNavigation(): any {
    return this.jQuery['setupVerticalNavigation'];
  }
}
