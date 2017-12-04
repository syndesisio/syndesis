import { Pipe, PipeTransform } from '@angular/core';

/**
 * Converts and normalizes a string to a valid and
 * standardized HTML 5 DOM id with slug format
 */
@Pipe({
  name: 'synSlugify'
})
export class SlugifyPipe implements PipeTransform {
  transform(value: string) {
    return value ? value.replace(/[^a-zA-Z0-9]+/g, '-').toLowerCase() : value;
  }
}
