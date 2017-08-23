import { Pipe } from '@angular/core';


@Pipe({
  name: 'toId',
})
/**
 * Converts and normalizes a string to a valid and standardized
 * HTML 5 DOM id
 */
export class ToIdPipe {
  transform(value: string, config: any) {
    if (!value) {
      return undefined;
    }
    return value.replace(' ', '-').toLowerCase();
  }
}

