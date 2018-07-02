import { of as observableOf, Observable } from 'rxjs';
import { Pipe, PipeTransform } from '@angular/core';
import { map } from 'rxjs/operators';

import { moment } from '@syndesis/ui/vendor';

@Pipe({
  name: 'synDurationDiff$'
})
export class DurationDiffPipe implements PipeTransform {
  transform(
    timestamp: number,
    defaultValue = 'n/a'
  ): Observable<string> | string {
    if (!timestamp) {
      return observableOf(defaultValue);
    }

    const startDate = moment(timestamp);
    const uptimeDuration = moment.duration(moment().diff(startDate));

    return observableOf(uptimeDuration).pipe(
      map(duration => ({
        days: duration.days(),
        hours: duration.hours(),
        minutes: duration.minutes()
      })),
      map(durationAsObject =>
        Object.keys(durationAsObject).reduce(
          (timeSpan: string, key: string) => {
            return durationAsObject[key] > 0
              ? timeSpan + `${durationAsObject[key]} ${key} `
              : timeSpan;
          },
          ''
        )
      ),
      map(
        durationString =>
          durationString && durationString.length > 0
            ? durationString
            : defaultValue
      )
    );
  }
}
