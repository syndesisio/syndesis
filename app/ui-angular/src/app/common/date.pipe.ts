import { Pipe, PipeTransform } from '@angular/core';
import { moment } from '@syndesis/ui/vendor';

export const DEFAULT_FORMAT = 'MMM Do LT';
export const SHORT = 'M/D/YYYY, LT'; // (6/15/15, 9:03 AM).
export const MEDIUM = 'MMM D, YYYY, LTS'; // (Jun 15, 2015, 9:03:01 AM).
export const LONG = 'MMMM D, YYYY, LTS z'; // (June 15, 2015 at 9:03:01 AM GMT+1).
export const FULL = 'EEEE, MMMM D, YYYY, LTS zzzz'; // (Monday, June 15, 2015 at 9:03:01 AM GMT+01:00).
export const SHORTDATE = 'M/D/YYYY'; // (6/15/15).
export const MEDIUMDATE = 'MMM D, YYYY'; // (Jun 15, 2015).
export const LONGDATE = 'MMMM D, YYYY'; // (June 15, 2015).
export const FULLDATE = 'EEEE, MMMM D, YYYY'; // (Monday, June 15, 2015).
export const SHORTTIME = 'LT'; // (9:03 AM).
export const MEDIUMTIME = 'LTS'; // (9:03:01 AM).
export const LONGTIME = 'LTS z'; // (9:03:01 AM GMT+1).
export const FULLTIME = 'LTS zzzz'; // (9:03:01 AM GMT+01:00).

type dateFormats =
  | string
  | 'time'
  | 'short'
  | 'medium'
  | 'long'
  | 'full'
  | 'shortDate'
  | 'mediumDate'
  | 'longDate'
  | 'fullDate'
  | 'shortTime'
  | 'mediumTime'
  | 'longTime'
  | 'fullTime';

@Pipe({
  name: 'synDate',
  pure: false
})
export class DatePipe implements PipeTransform {
  transform(value: number | string, format: dateFormats = DEFAULT_FORMAT): any {
    if (typeof value === 'undefined') {
      return undefined;
    }
    switch (format) {
      case 'time':
        format = 'YYYY-MM-DD HH:mm:SS A'; // useful for the <time> component
        break;
      case 'short':
        format = SHORT;
        break;
      case 'medium':
        format = MEDIUM;
        break;
      case 'long':
        format = LONG;
        break;
      case 'full':
        format = FULL;
        break;
      case 'shortDate':
        format = SHORTDATE;
        break;
      case 'mediumDate':
        format = MEDIUMDATE;
        break;
      case 'longDate':
        format = LONGDATE;
        break;
      case 'fullDate':
        format = FULLDATE;
        break;
      case 'shortTime':
        format = SHORTTIME;
        break;
      case 'mediumTime':
        format = MEDIUMTIME;
        break;
      case 'longTime':
        format = LONGTIME;
        break;
      case 'fullTime':
        format = FULLTIME;
        break;
      default:
        break;
    }
    return moment(value).format(format);
  }
}
