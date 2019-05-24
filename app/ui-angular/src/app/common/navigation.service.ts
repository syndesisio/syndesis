import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable()
export class NavigationService {
  collapsed$ = new BehaviorSubject<boolean>(false);

  toggle(newValue: boolean) {
    this.collapsed$.next(newValue);
  }

  show() {
    this.toggle(false);
  }

  hide() {
    this.toggle(true);
  }
}
