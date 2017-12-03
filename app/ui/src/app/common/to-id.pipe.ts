import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'toId'
})
/**
 * Converts and normalizes a string to a valid and standardized
 * HTML 5 DOM id
 * @TODO: Rename this to SlugPipe with selector 'slug'
 */
export class ToIdPipe implements PipeTransform {
  transform(value: string, config: any) {
    if (!value) {
      return undefined;
    }
    return value.replace(' ', '-').toLowerCase();
  }
}
