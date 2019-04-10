import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'words'
})
export class TruncateWordsPipe implements PipeTransform {
  transform(value: string, limit = 40, trail = '…'): string {
    if (!value) {
      return '';
    }

    let result = value;

    if (value) {
      const words = value.split(/\s+/);
      if (words.length > Math.abs(limit)) {
        if (limit < 0) {
          limit *= -1;
          result =
            trail + words.slice(words.length - limit, words.length).join(' ');
        } else {
          result = words.slice(0, limit).join(' ') + trail;
        }
      }
    }

    return result;
  }
}
