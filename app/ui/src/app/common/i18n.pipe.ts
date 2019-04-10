import { Pipe, PipeTransform } from '@angular/core';

import { I18NService } from '@syndesis/ui/platform';

@Pipe({
  name: 'synI18n',
  pure: false
})
export class I18NPipe implements PipeTransform {
  constructor(private i18nService: I18NService) {}

  transform(value: string, args: any[]): any {
    return this.i18nService.localize(value, args);
  }
}
