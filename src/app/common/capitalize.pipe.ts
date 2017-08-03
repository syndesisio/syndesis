import { Pipe } from '@angular/core';

@Pipe({
  name: 'capitalize',
})
export class CapitalizePipe {
  transform(value: string) {
    if (typeof value === 'string' && value && value.length > 0) {
      return value.charAt(0).toUpperCase() + value.slice(1);
    }
    return value;
  }
}
