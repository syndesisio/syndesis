import { Pipe, PipeTransform } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators';

import { moment } from '@syndesis/ui/vendor';

@Pipe({
  name: 'synDuration$'
})
export class DurationPipe implements PipeTransform {
  transform(timestamp: number, defaultValue = 'n/a'): Observable<string> | string {
    if (!timestamp) {
      return Observable.of(defaultValue);
    }

    const startDate = moment(timestamp);
    const uptimeDuration = moment.duration(moment().diff(startDate));

    return Observable.of(uptimeDuration).pipe(
      map(duration => ({
        days: duration.days(),
        hours: duration.hours(),
        minutes: duration.minutes(),
      })),
      map(durationAsObject => Object
        .keys(durationAsObject)
        .reduce((timeSpan: string, key: string) => {
          return durationAsObject[key] > 0 ? timeSpan + `${durationAsObject[key]} ${key} ` : timeSpan;
        }, '')),
      map(durationString => durationString && durationString.length > 0 ? durationString : defaultValue)
    );
  }
}
