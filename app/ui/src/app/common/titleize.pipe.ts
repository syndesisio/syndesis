import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'titleize'
})
export class TitleizePipe implements PipeTransform {
  transform(value: string, config: any) {
    const separator = config
      ? config.separator
        ? config.separator
        : ' '
      : ' ';
    if (typeof value === 'string' && value && value.length > 0) {
      const parts = value.split(separator);
      const answer = parts.map(p => {
        if (p.length > 3) {
          return p.charAt(0).toUpperCase() + p.slice(1);
        }
        return p;
      });
      return answer.join(' ');
    }
    return value;
  }
}
