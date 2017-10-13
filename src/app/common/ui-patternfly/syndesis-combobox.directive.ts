import { Directive, ElementRef, Input, AfterViewChecked } from '@angular/core';

@Directive({ selector: '[syndesisCombobox]' })
export class SyndesisComboboxDirective implements AfterViewChecked {

    constructor(private el: ElementRef) {
      this.el = el;
    }

    ngAfterViewChecked() {
       (<any>$(this.el.nativeElement)).combobox();
    }
}
