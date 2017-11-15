import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'truncate'
})
export class TruncateCharactersPipe implements PipeTransform {
  transform(value: string, limit = 40, trail = '…'): string {
    if (!value) {
      return '';
    }
    if (limit < 0) {
      limit *= -1;
      return value.length > limit
        ? trail + value.substring(value.length - limit, value.length)
        : value;
    } else {
      return value.length > limit ? value.substring(0, limit) + trail : value;
    }
  }
}
